import os

bind = "0.0.0.0:{}".format(os.environ.get("PORT"))
reload = False
accesslog = '-'
access_log_format = '%(h)s %(l)s %(u)s %(t)s "%(r)s" %(s)s %(b)s "%(f)s" "%(a)s"'
errorlog = 'error.log'
if(os.environ.get("ENV") == 'dev'):
    reload = True
