package me.nizheg.telegram.bot.chgk.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import me.nizheg.telegram.bot.chgk.dto.Property;
import me.nizheg.telegram.bot.chgk.repository.PropertyDao;
import me.nizheg.telegram.bot.service.PropertyService;

/**

 *
 * @author Nikolay Zhegalin
 */
@Service("propertyService")
@Transactional
public class PropertyServiceImpl implements PropertyService {

    @Autowired
    private PropertyDao propertyDao;

    @Override
    @Transactional(readOnly = true)
    public String getValue(String key) {
        if (propertyDao.isExist(key)) {
            return propertyDao.read(key).getValue();
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getLongValue(String key) {
        String stringValue = getValue(key);
        if (stringValue == null) {
            return null;
        }
        return Long.valueOf(stringValue);
    }

    @Override
    @Transactional(readOnly = true)
    public String getValueForChat(String key, Long chatId) {
        if (propertyDao.isExist(key, chatId)) {
            return propertyDao.readByKeyAndChatId(key, chatId).getValue();
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean getBooleanValueForChat(String key, Long chatId) {
        String stringValue = getValueForChat(key, chatId);
        if (stringValue == null) {
            return null;
        }
        return Boolean.valueOf(stringValue);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getIntegerValueForChat(String key, Long chatId) {
        String stringValue = getValueForChat(key, chatId);
        if (stringValue == null) {
            return null;
        }
        return Integer.valueOf(stringValue);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getLongValueForChat(String key, Long chatId) {
        String stringValue = getValueForChat(key, chatId);
        if (stringValue == null) {
            return null;
        }
        return Long.valueOf(stringValue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> readChatIdsByKeyAndValue(String key, String value) {
        return propertyDao.readChatIdsByKeyAndValue(key, value);
    }

    @Override
    public void setValue(String key, String value) {
        Property property = new Property();
        property.setKey(key);
        property.setValue(value);
        if (propertyDao.isExist(key)) {
            propertyDao.update(property);
        } else {
            propertyDao.create(property);
        }
    }

    @Override
    public void setValue(String key, Boolean value) {
        String stringValue = value == null ? null : value.toString();
        setValue(key, stringValue);
    }

    @Override
    public void setValue(String key, Integer value) {
        String stringValue = value == null ? null : value.toString();
        setValue(key, stringValue);
    }

    @Override
    public void setValue(String key, Long value) {
        String stringValue = value == null ? null : value.toString();
        setValue(key, stringValue);
    }

    @Override
    public void setValueForChat(String key, String value, Long chatId) {
        Property property = new Property();
        property.setKey(key);
        property.setValue(value);
        property.setChatId(chatId);
        if (propertyDao.isExist(key, chatId)) {
            propertyDao.update(property);
        } else {
            propertyDao.create(property);
        }
    }

    @Override
    public void setValueForChat(String key, Boolean value, Long chatId) {
        String stringValue = value == null ? null : value.toString();
        setValueForChat(key, stringValue, chatId);
    }

    @Override
    public void setValueForChat(String key, Integer value, Long chatId) {
        String stringValue = value == null ? null : value.toString();
        setValueForChat(key, stringValue, chatId);
    }

    @Override
    public void setValueForChat(String key, Long value, Long chatId) {
        String stringValue = value == null ? null : value.toString();
        setValueForChat(key, stringValue, chatId);
    }

    @Override
    public void copyProperties(Long fromChatId, Long toChatId) {
        propertyDao.copyProperties(fromChatId, toChatId);
    }
}
