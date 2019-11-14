FROM gcr.io/google-appengine/python
LABEL maintainer="Aleksi Vuorjoki <aleksi.vuorjoki@gmail.com>"


RUN virtualenv -p python3.7 /env
ENV VIRTUAL_ENV /env
ENV PATH /env/bin:$PATH

ADD requirements.txt /app/requirements.txt
RUN pip install -r /app/requirements.txt

# Add the application source code.
ADD . /app

EXPOSE $PORT
# Run a WSGI server to serve the application. gunicorn must be declared as
# a dependency in requirements.txt.
CMD gunicorn --config /app/conf/gunicorn_config.py app:app