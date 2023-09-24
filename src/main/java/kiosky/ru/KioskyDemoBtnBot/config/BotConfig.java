package kiosky.ru.KioskyDemoBtnBot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/*
* Всё что относится к конфигурации - в этом классе
* */
@Configuration
@Data
@PropertySource("application.properties") // Указываем, где находятся свойства, откуда будет считываться значения через @Value
public class BotConfig {
    @Value("${bot.name}")
    String botName;
    @Value("${bot.token}")
    String token;
}
