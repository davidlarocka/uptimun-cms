FROM httpd:2.4.57-alpine3.18

RUN mkdir -p /opt/mw/apache-test/logs
RUN mkdir /public

ADD ./config/custom.conf /usr/local/apache2/conf/httpd.conf
RUN ln -s  /usr/local/apache2/htdocs /public