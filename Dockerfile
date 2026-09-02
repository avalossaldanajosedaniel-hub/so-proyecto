FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY src/ /app/
RUN javac ServidorPedidos.java GeneradorPedidos.java
CMD ["java", "ServidorPedidos"]
