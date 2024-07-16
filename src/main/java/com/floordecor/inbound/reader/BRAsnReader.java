package com.floordecor.inbound.reader;

import com.floordecor.inbound.dto.br.*;
import com.supplychain.foundation.consts.FileConstants;
import com.supplychain.foundation.consts.JobConstants;
import com.supplychain.foundation.exception.custom.FileValidationException;
import com.supplychain.foundation.service.SFTPService;
import com.supplychain.foundation.utility.FileUtils;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class BRAsnReader implements ItemStreamReader<Document> {
    @Value("#{jobParameters}")
    private Map<String, String> jobParameters;
    @Autowired(required = false)
    @Qualifier("brAsnSftpService") private SFTPService brAsnSftpService;
    private final String xmlFilePath;
    private boolean isRead = false;
    public BRAsnReader(String xmlFilePath) {
        this.xmlFilePath = xmlFilePath;
    }

    public Document parseXmlToDocument(String xmlFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(new File(xmlFilePath));

            // Normalize the XML Structure
            doc.getDocumentElement().normalize();

            return parseDocument(doc.getDocumentElement());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Document parseDocument(Element docElement) {
        Document document = new Document();
        NodeList packingListNodes=docElement.getElementsByTagName("packing_list");
        List<PackingList> packingListList =new ArrayList<>();
        for(int i=0;i<packingListNodes.getLength();i++){
            packingListList.add(parsePackingList((Element)packingListNodes.item(i)));
        }
        document.setPackingList(packingListList);
        return document;
    }

    private PackingList parsePackingList(Element packingListElement) {
        PackingList packingList = new PackingList();
        packingList.setMessages(parseMessages((Element) packingListElement.getElementsByTagName("messages").item(0)));
        NodeList packingListHNodes = packingListElement.getElementsByTagName("packing_list_h");
        List<PackingListH> packingListHeaders = new ArrayList<>();
        for (int i = 0; i < packingListHNodes.getLength(); i++) {
            packingListHeaders.add(parsePackingListH((Element) packingListHNodes.item(i)));
        }
        packingList.setPackingListHeaders(packingListHeaders);
        return packingList;
    }

    private Messages parseMessages(Element messagesElement) {
        Messages messages = new Messages();
        messages.setStatus(messagesElement.getAttribute("status"));
        return messages;
    }

    private PackingListH parsePackingListH(Element packingListHElement) {
        PackingListH packingListH = new PackingListH();
        packingListH.setOwner(packingListHElement.getAttribute("owner"));
        packingListH.setPackingListNo(packingListHElement.getAttribute("packing_list_no"));
        packingListH.setSupplier(packingListHElement.getAttribute("supplier"));
        packingListH.setDeliverTo(packingListHElement.getAttribute("deliver_to"));
        packingListH.setCreateTs(packingListHElement.getAttribute("create_ts"));
        packingListH.setCreateUser(packingListHElement.getAttribute("create_user"));
        packingListH.setModifyTs(packingListHElement.getAttribute("modify_ts"));
        packingListH.setTotQty(packingListHElement.getAttribute("tot_qty"));
        packingListH.setStatus(packingListHElement.getAttribute("status"));
        packingListH.setDate5(packingListHElement.getAttribute("date5"));

        NodeList packingListDNodes = packingListHElement.getElementsByTagName("packing_list_d");
        List<PackingListD> packingListDList = new ArrayList<>();
        for (int i = 0; i < packingListDNodes.getLength(); i++) {
            packingListDList.add(parsePackingListD((Element) packingListDNodes.item(i)));
        }
        packingListH.setPackingListDetails(packingListDList);
        return packingListH;
    }

    private PackingListD parsePackingListD(Element packingListDElement) {
        PackingListD packingListD = new PackingListD();
        packingListD.setOwner(packingListDElement.getAttribute("owner"));
        packingListD.setPackingListNo(packingListDElement.getAttribute("packing_list_no"));
        packingListD.setRowNo(packingListDElement.getAttribute("row_no"));
        packingListD.setMemo3(packingListDElement.getAttribute("memo3"));
        packingListD.setMemo4(packingListDElement.getAttribute("memo4"));
        packingListD.setDate1(packingListDElement.getAttribute("date1"));
        packingListD.setItemNo(packingListDElement.getAttribute("item_no"));
        packingListD.setOrderNo(packingListDElement.getAttribute("order_no"));
        packingListD.setQty(packingListDElement.getAttribute("qty"));
        return packingListD;
    }

    @Override
    public synchronized Document read() throws Exception {
        if (!isRead) {
            isRead = true;
            Document result= parseXmlToDocument(xmlFilePath);
            //Validation for asn lines
            PackingListH packingListH = result.getPackingList().get(0).getPackingListHeaders().get(0);
            List<PackingListD> packingListDList = packingListH.getPackingListDetails();
            boolean isValid=false;
            Set<String> skippedAsnLines=new HashSet<>();
            for(PackingListD packingListD:packingListDList)
            {
                double qty=Double.parseDouble(packingListD.getQty());
                if(qty>0)
                    isValid=true;
                else
                    skippedAsnLines.add(packingListD.toString());
            }
            if (!isValid)
                throw new FileValidationException("All ASN Lines are invalid!!",new Exception())
                        .startLineNumber(6)
                        .endLineNumber(6+packingListDList.size())
                        .inputs(new ArrayList<>(skippedAsnLines));
            else if(skippedAsnLines.size()>0)
            {
                writeFileForFailedLines(new ArrayList<>(skippedAsnLines),jobParameters.get(JobConstants.INPUT_FILE_NAME));
                writerErrorFileInSftp();
            }
            return result;
        } else {
            return null; // End of file reached
        }
    }

    private void writeFileForFailedLines(final List<String> lines, final String filename) {
        Path filePath = Path.of(filename);
        String finalFilePath = FileUtils.getFailureDirPath(filePath, filePath.getFileName().toString());
        FileUtils.appendIntoFile(finalFilePath, String.join("\n", lines));
    }

    private void writerErrorFileInSftp()
    {
        Path inputPath = Path.of(jobParameters.get(JobConstants.INPUT_FILE_NAME));
        String fileName = inputPath.getFileName().toString();
        String localFailurePath = FileUtils.getFailureDirPath(inputPath, fileName);
        final File file = new File(localFailurePath);
        brAsnSftpService.uploadFile(
                file,
                FileUtils.generatePath(
                        FileConstants.ROOT_PATH_FORMAT,
                        jobParameters.get(JobConstants.SOURCE_DIRECTORY),
                        FileConstants.ERROR),
                FileUtils.getFailureFileName(fileName));
        FileUtils.deleteFileIfExist(file);
    }

}
