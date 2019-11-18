import os

bind = "0.0.0.0:{}".format(os.environ.get("PORT"))
reload = False
if(os.environ.get("ENV") == 'dev'):
    reload = True
