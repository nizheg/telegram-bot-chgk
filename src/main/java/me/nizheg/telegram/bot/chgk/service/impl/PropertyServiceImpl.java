package me.nizheg.telegram.bot.chgk.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.annotation.Nullable;

import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.chgk.dto.Property;
import me.nizheg.telegram.bot.chgk.repository.PropertyDao;
import me.nizheg.telegram.bot.service.PropertyService;

/**
 * @author Nikolay Zhegalin
 */
@RequiredArgsConstructor
@Service("propertyService")
@Transactional
public class PropertyServiceImpl implements PropertyService {

    private final PropertyDao propertyDao;
    private final PropertyResolver propertyResolver;

    @Override
    @Transactional(readOnly = true)
    @Nullable
    public String getValue(String key) {
        String value = propertyResolver.getProperty(key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        if (propertyDao.isExist(key)) {
            return propertyDao.read(key).getValue();
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    @Nullable
    public Long getLongValue(String key) {
        String stringValue = getValue(key);
        if (stringValue == null) {
            return null;
        }
        return Long.valueOf(stringValue);
    }

    @Nullable
    @Override
    public Integer getIntegerValue(String key) {
        String stringValue = getValue(key);
        if (stringValue == null) {
            return null;
        }
        return Integer.valueOf(stringValue);
    }

    @Override
    @Transactional(readOnly = true)
    @Nullable
    public String getValueForChat(String key, Long chatId) {
        if (propertyDao.isExist(key, chatId)) {
            return propertyDao.readByKeyAndChatId(key, chatId).getValue();
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    @Nullable
    public Boolean getBooleanValueForChat(String key, Long chatId) {
        String stringValue = getValueForChat(key, chatId);
        if (stringValue == null) {
            return null;
        }
        return Boolean.valueOf(stringValue);
    }

    @Override
    @Transactional(readOnly = true)
    @Nullable
    public Integer getIntegerValueForChat(String key, Long chatId) {
        String stringValue = getValueForChat(key, chatId);
        if (stringValue == null) {
            return null;
        }
        return Integer.valueOf(stringValue);
    }

    @Override
    @Transactional(readOnly = true)
    @Nullable
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

    @Override
    public void deletePropertiesForChat(Long chatId) {
        propertyDao.deleteByChatId(chatId);
    }
}
