package uz.real.appbotsecond.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramBotService {

    SendMessage useBot(long chatId);

    SendMessage saveUser(CallbackQuery callbackQuery, long chatId);

    SendMessage checkDate(String stringDate, Message message, String fullName, long birthId);

    SendMessage myImportantMessage(long chatId, CallbackQuery callbackQuery);

    SendMessage prev(String birthId, long chatId, CallbackQuery callbackQuery);

    SendMessage next(String birthId, long chatId, CallbackQuery callbackQuery);

    SendMessage add(long chatId);

    SendMessage edit(String birthId, long chatId);

    SendMessage delete(String birthId, long chatId);

    SendMessage successDelete(String birthId, long chatId);

    SendMessage notDelete(String birthId, long chatId);

    SendMessage viewAll(CallbackQuery callbackQuery, long chatId);

}
