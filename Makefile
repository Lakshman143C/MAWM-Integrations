#Potentially use make file to hide implementation of build/test/deploy/etc from jenkins
#So that Jenkinsfile can be generic enough to fit any project
PROJECT_NAME = item-inbound
IMAGE_REGISTRY = fdazregistry.azurecr.io
IMAGE_REPOSITORY = floordecor/inbound
IMAGE_NAME = "inbound"
IMAGE_TAG = latest
VERSION = 0.0.1
ENVIRONMENT = dev
FULL_IMAGE_NAME = ${IMAGE_REGISTRY}/${IMAGE_REPOSITORY}/${IMAGE_NAME}
PUSH=true


//TODO: Modify the docker build commands to align with Dockerfile - single stage no cache

.PHONY: build-docker
build-docker: build
#	Note: caching likely doesn't work for every case and may be using a pre-existing image in the repo that is not updated. Possibly needs
#	modification to cache the intermediate mvn package stage
	DOCKER_BUILDKIT=1 docker build --cache-from ${FULL_IMAGE_NAME}:latest --build-arg serviceName=${PROJECT_NAME} -t ${FULL_IMAGE_NAME}:${IMAGE_TAG} .

.PHONY: build-docker-quick
build-docker-quick:
	DOCKER_BUILDKIT=1 docker build --cache-from ${FULL_IMAGE_NAME}:latest --build-arg serviceName=${PROJECT_NAME} -t ${FULL_IMAGE_NAME}:${IMAGE_TAG} .

.PHONY: run-docker-quick
run-docker-quick:
	docker run -it ${FULL_IMAGE_NAME}:${IMAGE_TAG}

#Docker build and run quick
.PHONY: br
br: build-docker-quick run-docker-quick

.PHONY: build
build:
	mvn package && cd target && java -Djarmode=layertools -jar ${PROJECT_NAME}-${VERSION}.jar extract

.PHONY: extract-jar
extract-jar:
	cd target && java -Djarmode=layertools -jar ${PROJECT_NAME}-${VERSION}.jar extract

.PHONY: buildkit-build-and-push
buildkit-build-and-push:
	buildctl-daemonless.sh build \
       --frontend dockerfile.v0 \
       --local context=`pwd` \
       --local dockerfile=`pwd` \
       --output type=image,name=${FULL_IMAGE_NAME}:${IMAGE_TAG},push=${PUSH} \
       --export-cache type=local,dest=/cache,mode=max \
       --import-cache type=local,src=/cache

.PHONY: build-no-test
build-no-test:
	mvn package -Dmaven.test.skip=true

.PHONY: tag
tag:
	docker tag ${FULL_IMAGE_NAME}:${IMAGE_TAG} ${FULL_IMAGE_NAME}:latest

.PHONY: test
test:
	./mvnw test

.PHONY: security-scan
security-scan:
	./mvnw dependency-check:check

.PHONY: push
push: tag
	docker push ${FULL_IMAGE_NAME}:${IMAGE_TAG}
	docker push ${FULL_IMAGE_NAME}:latest

.PHONY: version
version:
	@GitVersion | jq -j '[.SemVer, .ShortSha|tostring] | join("-")'

.PHONY: fullImage
fullImage:
	@echo -n ${FULL_IMAGE_NAME}:${IMAGE_TAG}
