# keygen/Dockerfile
FROM alpine:latest

RUN apk add --no-cache openssl

COPY generate-keys.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/generate-keys.sh

VOLUME ["/keys"]

CMD ["/usr/local/bin/generate-keys.sh"]