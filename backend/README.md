# mcc_backend_test
The project runs on Google Cloud Flexible App Engine. The runtime is a custom docker image defined in the Dockerfile. Docker images are pushed to Google Container Registry (GCR). The image can then be deployed from GCR to the App Engine.

##Prerequisites for developing

###Google Cloud SDK
1. Install the latest Cloud SDK version: https://cloud.google.com/sdk/docs/
2. Install python extensions to Cloud SDK ``gcloud components install app-engine-python``
3. Run `gcloud init`
4. Login to Google Cloud when prompted
5. Select  project


###Firebase Admin connection
Get the private key file for firebase admin sdk and copy it to backend/
DO NOT ADD THE FILE TO VERSION CONTROL
#####Following steps need to be done only for a new Google Cloud Project
6. Enable docker registry: https://console.cloud.google.com/flows/enableapi?apiid=containerregistry.googleapis.com
7. gcloud app create --project=<project_name>

###Docker
Docker should be installed in the development environment

##Project is managed with a Makefile:
##### Build the container
make build

##### Build and publish the container
make release

##### Publish a container to GCR.
make publish

##### Deploy the latest image to Google Cloud Flexible App Engine.
make deploy

##### Run the container
make run

##### Build and run the container
make up

##### Stop the running container
make stop

##### Build the container with differnt config and deploy file
make cnf=another_config.env dpl=another_deploy.env build

##Project structure
├── Dockerfile
        
├── Makefile

   Defines the make commands
        
├── README.md
   
   this file

├── app.py

   Entrypoint for application

├── app.yaml
    
   Config file for Google Cloud Flexible App Engine

├── conf 
   
│   └── gunicorn_config.py    #gunicorn config files

├── config.env
    
   Environment variables for the application

├── deploy.env

   Environment variables for Makefile

├── flaskr
   
   App sources

│   └── __init__.py

├── requirements.txt

   Python library requirements
    
