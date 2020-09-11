FROM openjdk:8-jdk

ADD bot/build/libs/bot-all.jar /bot.jar

CMD ["/usr/bin/java", "-jar", "-server", "/bot.jar"]
