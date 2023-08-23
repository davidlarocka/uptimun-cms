FROM httpd:2.4.57-alpine3.18

RUN mkdir -p /opt/mw/apache-test/logs

ADD ./config/custom.conf /usr/local/apache2/conf/httpd.conf
#COPY htdocs /usr/local/apache2/htdocs