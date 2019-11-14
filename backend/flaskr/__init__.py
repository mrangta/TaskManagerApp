from flask import Flask, jsonify, make_response, current_app

def create_app():
    # create and configure the app
    app = Flask(__name__, instance_relative_config=True)

    @app.route('/projects/<project_id>/tasks', methods=['POST'])
    def tasks(project_id):
        return {'id': 'task_id'}, 201

    @app.route('/projects/<project_id>/tasks/<task_id>', methods=['PUT'])
    def taskWithId(project_id, task_id):
        return jsonify_no_content()

    @app.route('/projects/<project_id>/tasks/<task_id>/users', methods=['PUT'])
    def taskUsers(project_id, task_id):
        return jsonify_no_content()

    @app.route('/projects', methods=['POST'])
    def projects(project_id):
        return {'id': 'project_id'}, 201

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
