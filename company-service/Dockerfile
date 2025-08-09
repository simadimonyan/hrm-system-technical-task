FROM bellsoft/liberica-runtime-container:jdk-23-glibc

WORKDIR /app

RUN wget https://services.gradle.org/distributions/gradle-8.10-bin.zip -O /tmp/gradle.zip \
    && unzip /tmp/gradle.zip -d /opt/ \
    && rm /tmp/gradle.zip \
    && ln -s /opt/gradle-8.10 /opt/gradle

ENV GRADLE_HOME=/opt/gradle
ENV PATH=$GRADLE_HOME/bin:$PATH

COPY . .

EXPOSE 8082

ENTRYPOINT ["gradle", "bootRun", "--no-daemon"] 