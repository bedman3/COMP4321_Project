FROM maven:3.6.3-jdk-8
EXPOSE 8080
COPY . /backend
WORKDIR /backend
RUN curl -s -L -o miniconda_installer.sh \
https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-x86_64.sh \
 && sh miniconda_installer.sh -b
RUN /root/miniconda3/bin/python -m venv venv
RUN ./venv/bin/pip install scipy numpy
WORKDIR /backend
RUN mvn package
RUN rm -rf src/
RUN rm -f *.pdf

ENTRYPOINT ["java","-jar","/backend/target/searchEngine-0.0.1-SNAPSHOT.jar"]
