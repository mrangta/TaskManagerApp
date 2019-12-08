# import config.
# You can change the default config with `make cnf="config_special.env" build`
cnf ?= config.env
include $(cnf)
export $(shell sed 's/=.*//' $(cnf))

# import deploy config
# You can change the default deploy config with `make cnf="deploy_special.env" release`
dpl ?= deploy.env
include $(dpl)
export $(shell sed 's/=.*//' $(dpl))


# HELP
# This will output the help for each task
# thanks to https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
.PHONY: help

help: ## This help.
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

.DEFAULT_GOAL := help


# DOCKER TASKS
# Build the container
build: ## Build the container
	docker build -t $(APP_NAME) .

build-nc: ## Build the container without caching
	docker build --no-cache -t $(APP_NAME) .

run: ## Run container on port configured in `config.env`
	docker run -i -t --rm -p=$(PORT):$(PORT) \
		--name="$(APP_NAME)" \
		--mount type=bind,source=$(shell pwd)/,target=/app,readonly \
		--env-file ./config.env \
		$(APP_NAME)


up: build run ## Run container on port configured in `config.env` (Alias to run)


stop: ## Stop and remove a running container
	docker stop $(APP_NAME); docker rm $(APP_NAME)

release: build-nc publish ## Make a release by building and publishing the `{version}` and `latest` tagged containers to gcr

# Docker publish
publish: publish-latest publish-version ## Publish the `{version}` and `latest` tagged containers to gcr

publish-latest: tag-latest ## Publish the `latest` taged container to gcr
	@echo 'publish latest to $(DOCKER_REPO)'
	docker push $(DOCKER_REPO)/$(PROJECT_ID)/$(APP_NAME):latest

publish-version: tag-version ## Publish the `{version}` taged container to gcr
	@echo 'publish $(VERSION) to $(DOCKER_REPO)'
	docker push $(DOCKER_REPO)/$(PROJECT_ID)/$(APP_NAME):$(VERSION)

# Docker tagging
tag: tag-latest tag-version ## Generate container tags for the `{version}` and `latest` tags

tag-latest: ## Generate container `{version}` tag
	@echo 'create tag latest'
	docker tag $(APP_NAME) $(DOCKER_REPO)/$(PROJECT_ID)/$(APP_NAME):latest

tag-version: ## Generate container `latest` tag
	@echo 'create tag $(VERSION)'
	docker tag $(APP_NAME) $(DOCKER_REPO)/$(PROJECT_ID)/$(APP_NAME):$(VERSION)

version: ## Output the current version
	@echo $(VERSION)

deploy: ## Deploy :latest image from GCR to App Engine
	gcloud app deploy --quiet --image-url $(DOCKER_REPO)/$(PROJECT_ID)/$(APP_NAME):latest

browse: ## Open the app running on Google Cloud
	gcloud app browse

validate-endpoints: ## Validate openapi.yml for endpoints deployment
	gcloud endpoints services deploy openapi.yml --validate-only

deploy-endpoints: ## deploy openapi.yml to Google Cloud Endpoints
	gcloud endpoints services deploy openapi.yml

install-firebase-extension:
	firebase ext:install --params=conf/.env.firebase_extension_config storage-resize-images --project=mcc-fall-2019-g22

gcloud-setup:
	gcloud config set project $(PROJECT_ID)

full-deploy:
	make gcloud-setup
	make install-firebase-extension
	make release
	make deploy
	make deploy-endpoints