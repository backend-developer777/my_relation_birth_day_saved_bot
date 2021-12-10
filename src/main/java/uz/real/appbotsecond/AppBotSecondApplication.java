package uz.real.appbotsecond;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.real.appbotsecond.bot.MyConfigurationBot;


@SpringBootApplication
@EnableScheduling
public class AppBotSecondApplication {
    public static void main(String[] args) {

        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new MyConfigurationBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        SpringApplication.run(AppBotSecondApplication.class, args);
    }


}