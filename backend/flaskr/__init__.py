from flask import Flask, jsonify, make_response, current_app, request

import firebase_admin

from firebase_admin import credentials
from firebase_admin import db
import os

import datetime

def create_app():
    # create and configure the app
    app = Flask(__name__, instance_relative_config=True)
    cred = credentials.Certificate('/app/firebase-admin-sdk-key.json')

    # Initialize the app with a custom auth variable, limiting the server's access
    firebase_admin.initialize_app(cred, {
        'databaseURL': os.environ.get('DB_URL'),
        'databaseAuthVariableOverride': {
            'uid': 'mcc-g22-backend'
        }
    })


    @app.route('/projects/<project_id>/tasks', methods=['POST'])
    def tasks(project_id):
        data = request.get_json()
        tasks_ref = db.reference('/tasks')
        new_task_ref = tasks_ref.push()
        new_task_ref.set(data)
        new_task_id = new_task_ref.key
        project_tasks_ref = db.reference('/projects/').child(project_id).child('tasks')
        project_tasks_ref.update({new_task_id: True})
        return {'id': new_task_id }, 201

    @app.route('/projects/<project_id>/tasks/<task_id>', methods=['PUT'])
    def taskWithId(project_id, task_id):
        data = request.get_json()
        task_ref = db.reference('/tasks').child(task_id)
        task_ref.update(data)
        return jsonify_no_content()

    @app.route('/projects/<project_id>/tasks/<task_id>/users', methods=['PUT'])
    def taskUsers(project_id, task_id):
        return jsonify_no_content()

    @app.route('/projects', methods=['POST'])
    def projects():
        projects_ref = db.reference('/projects')

        data = request.get_json()
        data['admin'] = 'adminUser'
        data['creationDate'] = datetime.datetime.now().strftime("%Y-%m-%dT%H:%M:%S%z")
        new_project_ref = projects_ref.push()
        new_project_ref.set(data)
        return {'id': new_project_ref.key }, 201

    @app.route('/projects/<project_id>', methods=['DELETE'])
    def projectWithId(project_id):
        return jsonify_no_content()

    @app.route('/projects/<project_id>/members', methods=['PUT'])
    def projectMembers(project_id):
        return jsonify_no_content()


    return app

def jsonify_no_content():
    response = make_response('', 204)
    response.mimetype = current_app.config['JSONIFY_MIMETYPE']

    return response
