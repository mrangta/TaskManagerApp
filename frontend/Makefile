# HELP
# This will output the help for each task
# thanks to https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
.PHONY: help

help: ## This help.
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

.DEFAULT_GOAL := help


# DOCKER TASKS
# Build the container
build: ## Build the building container
	docker build -t androidbuilder .

build-app: ## Run container on port configured in `config.env`
	mkdir -p app/build/outputs/apk
	docker run -i -t --rm --name="androidbuilder" \
		--mount type=bind,source=$(shell pwd)/app/build/outputs/apk,target=/application/app/build/outputs/apk/ \
		androidbuilder ./gradlew assembleDebug

container-bash: ## Run container on port configured in `config.env`
	mkdir -p app/build/outputs/apk
	docker run -i -t --rm --name="androidbuilder" \
		androidbuilder /bin/bash

stop: ## Stop and remove a running container
	docker stop androidbuilder; docker rm androidbuilder
