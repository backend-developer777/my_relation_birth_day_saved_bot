package uz.real.appbotsecond.service;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.real.appbotsecond.model.BirthDay;
import uz.real.appbotsecond.payload.ResBirthDay;
import uz.real.appbotsecond.repository.BirthDayRepository;
import uz.real.appbotsecond.repository.UserRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TelegramBotServiceImpl implements TelegramBotService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BirthDayRepository birthDayRepository;

    @Override
    public SendMessage useBot(long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Muhim sanalarni qo'shish!");
        inlineKeyboardButton.setCallbackData("Muhim sanalarni qo'shish!");
        List<InlineKeyboardButton> keyboardButtonRowOne = new LinkedList<>();
        keyboardButtonRowOne.add(inlineKeyboardButton);

        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Muhim sanalarim!");
        inlineKeyboardButton1.setCallbackData("Muhim sanalarim!");
        List<InlineKeyboardButton> keyboardButtonRowTwo = new LinkedList<>();
        keyboardButtonRowTwo.add(inlineKeyboardButton1);
        List<List<InlineKeyboardButton>> inlineRows = new LinkedList<>();
        inlineRows.add(keyboardButtonRowOne);
        inlineRows.add(keyboardButtonRowTwo);
        inlineKeyboardMarkup.setKeyboard(inlineRows);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Assalomu alaykum, yaqinlaringizni tug'ilgan kunini saqlab boruvchi botga hush kelibsiz!");
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    @Override
    public SendMessage saveUser(CallbackQuery callbackQuery, long chatId) {
        SendMessage sendMessage = new SendMessage();
        User from = callbackQuery.getFrom();
        Optional<uz.real.appbotsecond.model.User> byUsername = userRepository.findByChatId(String.valueOf(from.getId()));
        if (byUsername.isPresent()) {
            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setText("Siz avval ham ushbu botdan foydalangansiz, marxamat do'stingizning ismi va familyasini kiriting! \nmasalan Sherzod Nurmatov ko'rinishida");
            return sendMessage;
        }
        uz.real.appbotsecond.model.User user = new uz.real.appbotsecond.model.User();
        user.setFirstName(from.getFirstName());
        user.setUsername(from.getUserName());
        user.setChatId(String.valueOf(chatId));
        userRepository.save(user);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Do'stingizning ismi va familyasini kiriting masalan: Sherzod Nurmatov ko'rinishida!");
        return sendMessage;
    }

    @Override
    public SendMessage checkDate(String stringDate, Message message, String fullName, long birthId) {
        SendMessage sendMessage = new SendMessage();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Yana do'stlarni saqlash!");
        inlineKeyboardButton.setCallbackData("Yana do'stlarni saqlash!");
        inlineKeyboardButton1.setText("Muhim sanalarim!");
        inlineKeyboardButton1.setCallbackData("Muhim sanalarim!");
        List<InlineKeyboardButton> keyboardButtonList = new LinkedList<>();
        List<InlineKeyboardButton> inlineKeyboardButtons = new LinkedList<>();
        keyboardButtonList.add(inlineKeyboardButton);
        inlineKeyboardButtons.add(inlineKeyboardButton1);
        List<List<InlineKeyboardButton>> inlineRows = new LinkedList<>();
        inlineRows.add(keyboardButtonList);
        inlineRows.add(inlineKeyboardButtons);
        inlineKeyboardMarkup.setKeyboard(inlineRows);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        int day = Integer.parseInt(stringDate.substring(0, 2));
        int month = Integer.parseInt(stringDate.substring(3, 5));
        int year = Integer.parseInt(stringDate.substring(6, stringDate.length()));

        if (day >= 1 && day <= 31) {
            if (month >= 1 && month <= 12) {
                if (year > LocalDate.now().getYear() - 120 && year < LocalDate.now().getYear()) {
                    LocalDate birthDayDate = LocalDate.parse(stringDate, formatter);
                    if (birthId != 0) {
                        Optional<BirthDay> byId = birthDayRepository.findById(birthId);
                        if (byId.isPresent()) {
                            BirthDay birthDay = byId.get();
                            birthDay.setId(birthId);
                            birthDay.setFullName(fullName);
                            birthDay.setBirthDayDate(birthDayDate);
                            birthDay.setUser(userRepository.getByChatId(String.valueOf(message.getFrom().getId())));
                            birthDayRepository.save(birthDay);
                            sendMessage.setChatId(String.valueOf(message.getChatId()));
                            sendMessage.setText("Muvafaqqiyatli o'zgartirildi! \n Ismi va familyasi:" + birthDay.getFullName() + "\nTug'ilgan sanasi: " + birthDay.getBirthDayDate() + " ga o'zgardi!");
                            sendMessage.setReplyMarkup(crudAndPaginationButtons(birthId));
                            return sendMessage;
                        }
                    }
                    BirthDay savedBirthDay = new BirthDay();
                    savedBirthDay.setBirthDayDate(birthDayDate);
                    savedBirthDay.setFullName(fullName);
                    savedBirthDay.setUser(userRepository.getByUsername(message.getFrom().getUserName()));
                    birthDayRepository.save(savedBirthDay);
                    sendMessage.setChatId(String.valueOf(message.getChatId()));
                    sendMessage.setText("Tabriklaymiz siz do'stingizni tug'ilgan kunini muvafaqqiyatli saqladingiz, botning o'zi sizga xabar yuboradi!");
                    sendMessage.setReplyMarkup(inlineKeyboardMarkup);
                    return sendMessage;
                } else {
                    sendMessage.setChatId(String.valueOf(message.getChatId()));
                    sendMessage.setText("Yilni to'g'ri kiriting, yil " + (LocalDate.now().getYear() + 1) + " dan kichik bo'lishi kerak. Siz " + year + " kiritdingiz!");
                    return sendMessage;
                }
            } else {
                sendMessage.setChatId(String.valueOf(message.getChatId()));
                sendMessage.setText("Oyni to'g'ri kiriting, oy (1-12) oralig'ida bo'lishi kerak! Siz " + month + " kiritdingiz!");
                return sendMessage;
            }
        } else {
            sendMessage.setChatId(String.valueOf(message.getChatId()));
            sendMessage.setText("Kunni to'g'ri kiriting, kun (1-31) oralig'ida bo'lishi kerak! Siz " + day + " kiritdingiz!");
            return sendMessage;
        }
    }

    @Override
    public SendMessage myImportantMessage(long chatId, CallbackQuery callbackQuery) {
        SendMessage sendMessage = new SendMessage();
        User from = callbackQuery.getFrom();
        Optional<uz.real.appbotsecond.model.User> byUsername = userRepository.findByChatId(String.valueOf(from.getId()));
        if (byUsername.isPresent()) {
            uz.real.appbotsecond.model.User user = byUsername.get();
            List<BirthDay> birthDayList = birthDayRepository.getAllByUserId(user.getId());
            if (birthDayList.size() > 0) {
                BirthDay birthDay = birthDayList.get(birthDayList.size() - 1);
                long currentId = birthDay.getId();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("Ismi va familyasi:  " + birthDay.getFullName() + "\nTug'ilgan sanasi:  " + birthDay.getBirthDayDate());
                sendMessage.setReplyMarkup(crudAndPaginationButtons(currentId));
                return sendMessage;
            }
        }
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Siz hozircha do'stlaringizni tug'ilgan kunini saqlamagansiz, 'Muhim sanalarni qo'shish!' tugmachasini tanlang!");
        return sendMessage;
    }

    @Override
    @Transactional
    public SendMessage prev(String birthId, long chatId, CallbackQuery callbackQuery) {
        SendMessage sendMessage = new SendMessage();
        long currentBirthDayId = Long.parseLong(birthId.substring(1));
            List<BirthDay> allByUserId = birthDayRepository.findAllByUser_ChatId(String.valueOf(callbackQuery.getFrom().getId()));
            if (allByUserId.size() > 0) {
                long firstBirthDayId = allByUserId.get(0).getId();
                long lastBirthDayId = allByUserId.get(allByUserId.size() - 1).getId();
                if (currentBirthDayId > firstBirthDayId && currentBirthDayId <= lastBirthDayId) {
                    long prev = currentBirthDayId - 1;
                    Optional<BirthDay> birthDayOptional = birthDayRepository.findById(prev);
                    if (birthDayOptional.isPresent()) {
                        BirthDay birthDay = birthDayOptional.get();
                        sendMessage.setChatId(String.valueOf(chatId));
                        sendMessage.setText("Tug'ilgan sanasi:  " + birthDay.getBirthDayDate() + "\nIsmi va familyasi:  " + birthDay.getFullName());
                        sendMessage.setReplyMarkup(crudAndPaginationButtons(prev));
                        return sendMessage;
                    } else {
                        while (firstBirthDayId != prev) {
                            prev--;
                            Optional<BirthDay> byId = birthDayRepository.findById(prev);
                            if (byId.isPresent()) {
                                BirthDay birthDay = byId.get();
                                sendMessage.setChatId(String.valueOf(chatId));
                                sendMessage.setText("Tug'ilgan sanasi:  " + birthDay.getBirthDayDate() + "\nIsmi va familyasi:  " + birthDay.getFullName());
                                sendMessage.setReplyMarkup(crudAndPaginationButtons(prev));
                                return sendMessage;
                            }
                        }
                    }
                }else if (currentBirthDayId == firstBirthDayId){
                    Optional<BirthDay> byId = birthDayRepository.findById(currentBirthDayId);
                    if (byId.isPresent()){
                        BirthDay birthDay = byId.get();
                        sendMessage.setChatId(String.valueOf(chatId));
                        sendMessage.setText("Tug'ilgan sanasi: " + birthDay.getBirthDayDate() + "\nIsmi va familyasi:  " + birthDay.getFullName());
                        sendMessage.setReplyMarkup(firstBirthDayBTN(currentBirthDayId));
                    }else {
                        sendMessage.setChatId(String.valueOf(chatId));
                        sendMessage.setText("iz hamma do'stlaringizni o'chirib bo'lgansiz! /start");
                        sendMessage.setReplyMarkup(firstBirthDayBTN(currentBirthDayId));
                    } }
                }
        else {
            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setText("Siz hamma do'stlaringizni o'chirib bo'lgansiz! /start");
            return sendMessage;
        }
        return null;
    }

    private InlineKeyboardMarkup firstBirthDayBTN(long birthId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButtonOld1 = new InlineKeyboardButton();
        inlineKeyboardButtonOld1.setText("Keyingi >>");
        inlineKeyboardButtonOld1.setCallbackData("n" + birthId);
        inlineKeyboardButton.setText("O'chirish!");
        inlineKeyboardButton.setCallbackData("d" + birthId);
        inlineKeyboardButton1.setText("Qo'shish!");
        inlineKeyboardButton1.setCallbackData("add");
        inlineKeyboardButton2.setText("O'zgartirish!");
        inlineKeyboardButton2.setCallbackData("e" + birthId);
        inlineKeyboardButton3.setText("Barchasini ko'rish!");
        inlineKeyboardButton3.setCallbackData("view");
        List<InlineKeyboardButton> keyboardButtonList = new LinkedList<>();
        List<InlineKeyboardButton> keyboardButtonListOld = new LinkedList<>();
        List<InlineKeyboardButton> keyboardButtonListEditAndView = new LinkedList<>();
        keyboardButtonListOld.add(inlineKeyboardButtonOld1);
        keyboardButtonList.add(inlineKeyboardButton);
        keyboardButtonList.add(inlineKeyboardButton1);
        keyboardButtonListEditAndView.add(inlineKeyboardButton2);
        keyboardButtonListEditAndView.add(inlineKeyboardButton3);
        List<List<InlineKeyboardButton>> inlineRows = new LinkedList<>();
        inlineRows.add(keyboardButtonListOld);
        inlineRows.add(keyboardButtonList);
        inlineRows.add(keyboardButtonListEditAndView);
        inlineKeyboardMarkup.setKeyboard(inlineRows);
        return inlineKeyboardMarkup;
    }

    @Override
    public SendMessage next(String birthId, long chatId, CallbackQuery callbackQuery) {
        SendMessage sendMessage = new SendMessage();
        long currentBirthId = Long.parseLong(birthId.substring(1));
        List<BirthDay> allByUser_chatId = birthDayRepository.findAllByUser_ChatId(String.valueOf(callbackQuery.getFrom().getId()));
        if (allByUser_chatId.size()>0){
            long lastBirthId = allByUser_chatId.get(allByUser_chatId.size() - 1).getId();
            if (currentBirthId < lastBirthId) {
                long nextBirthId = currentBirthId + 1;
                Optional<BirthDay> birthDayOptional = birthDayRepository.findById(nextBirthId);
                if (birthDayOptional.isPresent()) {
                    BirthDay birthDay = birthDayOptional.get();
                    sendMessage.setChatId(String.valueOf(chatId));
                    sendMessage.setText("Tug'ilgan sanasi: " + birthDay.getBirthDayDate() + "\n + Ismi va familiyasi: " + birthDay.getFullName());
                    sendMessage.setReplyMarkup(crudAndPaginationButtons(nextBirthId));
                    return sendMessage;
                } else {
                    while (nextBirthId != lastBirthId) {
                        nextBirthId++;
                        Optional<BirthDay> byId = birthDayRepository.findById(nextBirthId);
                        if (byId.isPresent()) {
                            sendMessage.setChatId(String.valueOf(chatId));
                            sendMessage.setText("Tug'ilgan sanasi:  " + byId.get().getBirthDayDate() + "\n Ismi va familiyasi:  " + byId.get().getFullName());
                            sendMessage.setReplyMarkup(crudAndPaginationButtons(nextBirthId));
                            return sendMessage;
                        }
                    }
                }
            }
            else if (currentBirthId == lastBirthId) {
                Optional<BirthDay> birthDayNextOptional = birthDayRepository.findById(currentBirthId);
                if (birthDayNextOptional.isPresent()) {
                    BirthDay birthDay = birthDayNextOptional.get();
                    sendMessage.setChatId(String.valueOf(chatId));
                    sendMessage.setText("Tug'ilgan sanasi:  " + birthDay.getBirthDayDate() + "\nIsmi va familiyasi:  " + birthDay.getFullName());
                    sendMessage.setReplyMarkup(lastBirthDayBTN(currentBirthId));
                    return sendMessage;
                }

            }
        }
        return null;
    }

    private InlineKeyboardMarkup lastBirthDayBTN(long birthId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButtonOld1 = new InlineKeyboardButton();
        inlineKeyboardButtonOld1.setText("<< Oldingi");
        inlineKeyboardButtonOld1.setCallbackData("p" + birthId);
        inlineKeyboardButton.setText("O'chirish!");
        inlineKeyboardButton.setCallbackData("d" + birthId);
        inlineKeyboardButton1.setText("Qo'shish!");
        inlineKeyboardButton1.setCallbackData("add");
        inlineKeyboardButton2.setText("O'zgartirish!");
        inlineKeyboardButton2.setCallbackData("e" + birthId);
        inlineKeyboardButton3.setText("Barchasini ko'rish!");
        inlineKeyboardButton3.setCallbackData("view");
        List<InlineKeyboardButton> keyboardButtonList = new LinkedList<>();
        List<InlineKeyboardButton> keyboardButtonListOld = new LinkedList<>();
        List<InlineKeyboardButton> keyboardButtonListEditAndView = new LinkedList<>();
        keyboardButtonListOld.add(inlineKeyboardButtonOld1);
        keyboardButtonList.add(inlineKeyboardButton);
        keyboardButtonList.add(inlineKeyboardButton1);
        keyboardButtonListEditAndView.add(inlineKeyboardButton2);
        keyboardButtonListEditAndView.add(inlineKeyboardButton3);
        List<List<InlineKeyboardButton>> inlineRows = new LinkedList<>();
        inlineRows.add(keyboardButtonListOld);
        inlineRows.add(keyboardButtonList);
        inlineRows.add(keyboardButtonListEditAndView);
        inlineKeyboardMarkup.setKeyboard(inlineRows);
        return inlineKeyboardMarkup;


    }

    @Override
    public SendMessage add(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Ism va familyangizni kiriting kiriting! Masalan Sherzod Nurmatov ko'rinishida!");
        return sendMessage;
    }

    @Override
    public SendMessage edit(String birthId, long chatId) {
        SendMessage sendMessage = new SendMessage();
        long currentBirthId = Long.parseLong(birthId.substring(1));
        BirthDay birthDay = birthDayRepository.findById(currentBirthId).get();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(birthDay.getFullName() + " ni o'zgartirmoqchisiz, yangi ism va familiya kiriting!");
        return sendMessage;
    }

    @Override
    public SendMessage delete(String birthId, long chatId) {
        SendMessage sendMessage = new SendMessage();
        long currentBirthId = Long.parseLong(birthId.substring(1));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Ha");
        inlineKeyboardButton.setCallbackData("Ha" + currentBirthId);
        inlineKeyboardButton1.setText("Yo'q");
        inlineKeyboardButton1.setCallbackData("Yo" + currentBirthId);
        List<InlineKeyboardButton> keyboardButtonList = new LinkedList<>();
        keyboardButtonList.add(inlineKeyboardButton);
        keyboardButtonList.add(inlineKeyboardButton1);
        List<List<InlineKeyboardButton>> inlineRows = new LinkedList<>();
        inlineRows.add(keyboardButtonList);
        inlineKeyboardMarkup.setKeyboard(inlineRows);
        if (birthDayRepository.findById(currentBirthId).isPresent()) {
            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setText("Rostan ham o'chirmoqchimisiz!");
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            return sendMessage;
        }
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Belgilagan do'stlaringizni hammasini o'chirib bo'ldingiz!");
        sendMessage.setReplyMarkup(crudAndPaginationButtons(currentBirthId));
        return sendMessage;

    }

    @Override
    public SendMessage successDelete(String birthId, long chatId) {
        SendMessage sendMessage = new SendMessage();
        long currentBirthId = Long.parseLong(birthId.substring(2));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Orqaga!");
        button.setCallbackData("Orqaga!");
        List<InlineKeyboardButton> inlineKeyboardButtonList = new LinkedList<>();
        inlineKeyboardButtonList.add(button);
        List<List<InlineKeyboardButton>> inlineRows = new LinkedList<>();
        inlineRows.add(inlineKeyboardButtonList);
        inlineKeyboardMarkup.setKeyboard(inlineRows);
        birthDayRepository.deleteById(currentBirthId);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Muvafaqqiyatli o'chirildi!");
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    @Override
    public SendMessage notDelete(String birthId, long chatId) {
        SendMessage sendMessage = new SendMessage();
        long currentBirthId = Long.parseLong(birthId.substring(2));
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Amaliyot bajarilmadi");
        sendMessage.setReplyMarkup(crudAndPaginationButtons(currentBirthId));
        return sendMessage;
    }

    @Override
    public SendMessage viewAll(CallbackQuery callbackQuery, long chatId) {
        SendMessage sendMessage = new SendMessage();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Orqaga!");
        inlineKeyboardButton.setCallbackData("Orqaga!");
        List<InlineKeyboardButton> keyboardButtonList = new LinkedList<>();
        keyboardButtonList.add(inlineKeyboardButton);
        List<List<InlineKeyboardButton>> inlineRows = new LinkedList<>();
        inlineRows.add(keyboardButtonList);
        inlineKeyboardMarkup.setKeyboard(inlineRows);
        List<BirthDay> allByUser_chatId = birthDayRepository.findAllByUser_ChatId(String.valueOf(callbackQuery.getFrom().getId()));
        if (allByUser_chatId.size() > 0) {
            List<ResBirthDay> resBirthDayList = new LinkedList<>();
            for (BirthDay birthDay : allByUser_chatId) {
                resBirthDayList.add(new ResBirthDay(birthDay.getFullName(), birthDay.getBirthDayDate()));
            }
            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setText("" + resBirthDayList.stream().map(resBirthDay -> "Ismi va familyasi:   " + resBirthDay.getFullName() + " \nTug'ilgan sanasi:   " + resBirthDay.getBirthDayDate() + "\n\n").collect(Collectors.joining()));
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            return sendMessage;

        }
        sendMessage.setText(String.valueOf(chatId));
        sendMessage.setText("Avval do'stlaringizni belgilang!");
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    private InlineKeyboardMarkup crudAndPaginationButtons(long birthId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButtonOld1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButtonOld2 = new InlineKeyboardButton();
        inlineKeyboardButtonOld1.setText("<< Oldingi");
        inlineKeyboardButtonOld1.setCallbackData("p" + birthId);
        inlineKeyboardButtonOld2.setText("Keyingi >>");
        inlineKeyboardButtonOld2.setCallbackData("n" + birthId);
        inlineKeyboardButton.setText("O'chirish!");
        inlineKeyboardButton.setCallbackData("d" + birthId);
        inlineKeyboardButton1.setText("Qo'shish!");
        inlineKeyboardButton1.setCallbackData("add");
        inlineKeyboardButton2.setText("O'zgartirish!");
        inlineKeyboardButton2.setCallbackData("e" + birthId);
        inlineKeyboardButton3.setText("Barchasini ko'rish!");
        inlineKeyboardButton3.setCallbackData("view");
        List<InlineKeyboardButton> keyboardButtonList = new LinkedList<>();
        List<InlineKeyboardButton> keyboardButtonListOld = new LinkedList<>();
        List<InlineKeyboardButton> keyboardButtonListEditAndView = new LinkedList<>();
        keyboardButtonListOld.add(inlineKeyboardButtonOld1);
        keyboardButtonListOld.add(inlineKeyboardButtonOld2);
        keyboardButtonList.add(inlineKeyboardButton);
        keyboardButtonList.add(inlineKeyboardButton1);
        keyboardButtonListEditAndView.add(inlineKeyboardButton2);
        keyboardButtonListEditAndView.add(inlineKeyboardButton3);
        List<List<InlineKeyboardButton>> inlineRows = new LinkedList<>();
        inlineRows.add(keyboardButtonListOld);
        inlineRows.add(keyboardButtonList);
        inlineRows.add(keyboardButtonListEditAndView);
        inlineKeyboardMarkup.setKeyboard(inlineRows);
        return inlineKeyboardMarkup;
    }

}
