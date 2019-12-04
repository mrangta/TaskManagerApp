from flask import Flask
from flaskr.apiV1 import api_v1


def create_app():
    # create and configure the app
    app = Flask(__name__, instance_relative_config=True)
    app.register_blueprint(api_v1, url_prefix='/v1')
    return app

