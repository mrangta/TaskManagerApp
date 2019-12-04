import datetime
import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
from flask import Flask, make_response, current_app, request
import os


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

    @app.route('/alive', methods=['GET'])
    def alive():
        return {'status': 'OK'}, 200

    @app.route('/tasks', methods=['POST'])
    def tasks():
        data = request.get_json()
        tasks_ref = db.reference('/tasks')
        new_task_ref = tasks_ref.push()
        new_task_ref.set(data)
        new_task_id = new_task_ref.key
        project_id = data.get('projectId')
        project_tasks_ref = db.reference('/projects/').child(project_id).child('tasks')
        project_tasks_ref.update({new_task_id: True})
        log_ref = db.reference('/log')
        log_event_ref = log_ref.child(project_id).child(new_task_id).push()
        log_event_ref.set({'type': 'CREATED', 'description': 'Task Created', 'timestamp': datetime.datetime.utcnow().replace(microsecond=0).isoformat() + 'Z'})
        return {'id': new_task_id }, 201

    @app.route('/tasks/<task_id>', methods=['PUT'])
    def task_with_id(task_id):
        data = request.get_json()
        task_ref = db.reference('/tasks').child(task_id)
        task_ref.update(data)
        log_ref = db.reference('/log')
        project_id = task_ref.child('projectId').get()
        log_event_ref = log_ref.child(project_id).child(task_id).push()
        log_event_ref.set({'type': 'STATUS', 'description': 'Task status changed to ' + data['status'],
                           'timestamp': datetime.datetime.utcnow().replace(microsecond=0).isoformat() + 'Z'})

        return jsonify_no_content()

    @app.route('/tasks/<task_id>/users', methods=['PUT'])
    def task_users(task_id):
        user_ids = request.get_json().get('userIds', [])
        array_as_dict = array_to_fb_object(user_ids)
        task_ref = db.reference('/tasks').child(task_id)
        task_users_ref = task_ref.child('users')
        task_users_ref.update(array_as_dict)
        log_ref = db.reference('/log')
        project_id = task_ref.child('projectId').get()
        for user_id in user_ids:
            user_ref = db.reference('/users').child(user_id)
            user_tasks_ref = user_ref.child('tasks')
            user_tasks_ref.update({task_id: True})
            log_event_ref = log_ref.child(project_id).child(task_id).push()
            log_event_ref.set({'type': 'ASSIGNMENT', 'description': 'User ' + user_ref.child('name').get() + ' assigned to task',
                               'timestamp': datetime.datetime.utcnow().replace(microsecond=0).isoformat() + 'Z'})
            return jsonify_no_content()

    @app.route('/projects', methods=['POST'])
    def projects():
        projects_ref = db.reference('/projects')
        data = request.get_json()
        keywords = data.get('keywords')
        if keywords is not None:
            data['keywords'] = array_to_fb_object(keywords)
        data['admin'] = 'adminUser'
        date = datetime.datetime.utcnow().replace(microsecond=0).isoformat() + 'Z'
        data['creationDate'] = date
        data['lastModifiedDate'] = date
        new_project_ref = projects_ref.push()
        new_project_ref.set(data)
        return {'id': new_project_ref.key }, 201

    @app.route('/projects/<project_id>', methods=['DELETE'])
    def project_with_id(project_id):

        project_ref = db.reference('/projects').child(project_id)
        task_ids = project_ref.child('tasks').get().keys()
        users_in_project = project_ref.child('members').get().keys()
        for task_id in task_ids:
            task_ref = db.reference('/tasks').child(task_id)
            users_with_task = task_ref.child('users').get().keys()
            for user_id in users_with_task:
                user_task_ref = db.reference('/users').child(user_id).child('tasks').child(task_id)
                delete_resource(user_task_ref)
            delete_resource(task_ref)
        for user_id in users_in_project:
            user_project_ref = db.reference('/users').child(user_id).child('projects').child(project_id)
            delete_resource(user_project_ref)
        delete_resource(project_ref)
        return jsonify_no_content()

    @app.route('/projects/<project_id>/members', methods=['PUT'])
    def project_members(project_id):
        user_ids = request.get_json().get('userIds', [])
        array_as_dict = array_to_fb_object(user_ids)
        project_members_ref = db.reference('/projects').child(project_id).child('members')
        project_members_ref.update(array_as_dict)
        for user_id in user_ids:
            user_tasks_ref = db.reference('/users').child(user_id).child('projects')
            user_tasks_ref.update({project_id: True})
        return jsonify_no_content()

    return app


def jsonify_no_content():
    response = make_response('', 204)
    response.mimetype = current_app.config['JSONIFY_MIMETYPE']

    return response


def delete_resource(db_ref):
    db_ref.set({})


def array_to_fb_object(array):
    return dict.fromkeys(array, True)
