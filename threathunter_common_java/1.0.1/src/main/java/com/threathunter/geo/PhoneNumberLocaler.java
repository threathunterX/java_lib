package com.threathunter.geo;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberToCarrierMapper;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;

import java.util.Locale;
import java.util.Random;

/**
 * created by www.threathunter.cn
 */
public class PhoneNumberLocaler {

    private static PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    private static PhoneNumberToCarrierMapper carrierMapper = PhoneNumberToCarrierMapper.getInstance();

    private static PhoneNumberOfflineGeocoder geocoder = PhoneNumberOfflineGeocoder.getInstance();

    public static boolean checkPhoneNumber(String phoneNumber, String countryCode){

        int ccode = Integer.parseInt(countryCode);
        long phone = Long.parseLong(phoneNumber);

        Phonenumber.PhoneNumber pn = new Phonenumber.PhoneNumber();
        pn.setCountryCode(ccode);
        pn.setNationalNumber(phone);

        return phoneNumberUtil.isValidNumber(pn);

    }

    public static String getCarrier(String phoneNumber, String countryCode){

        int ccode = Integer.parseInt(countryCode);
        long phone = Long.parseLong(phoneNumber);

        Phonenumber.PhoneNumber pn = new Phonenumber.PhoneNumber();
        pn.setCountryCode(ccode);
        pn.setNationalNumber(phone);
        //convert to chinese
        String carrierEn = carrierMapper.getNameForNumber(pn, Locale.ENGLISH);
        String des = geocoder.getDescriptionForNumber(pn, Locale.CHINESE);
        String carrier = "";
        switch (carrierEn) {
            case "China Mobile":
                carrier = "移动";
                break;
            case "China Unicom":
                carrier = "联通";
                break;
            case "China Telecom":
                carrier = "电信";
                break;
            default:
                break;
        }
        return String.format("%s\t%s", des, carrier);
    }

    public static String getGeo(String phoneNumber) {
        Phonenumber.PhoneNumber pn;
        String country;
        String descriptionForNumber;
        if (phoneNumber.startsWith("+")) {
            try {
                pn = PhoneNumberUtil.getInstance().parse(phoneNumber, "CN");
                String code = PhoneNumberUtil.getInstance().getRegionCodeForNumber(pn);
                Locale locale = new Locale("zh", code);
                country = locale.getDisplayCountry(Locale.SIMPLIFIED_CHINESE);
                descriptionForNumber = geocoder.getDescriptionForNumber(pn, locale);
            } catch (Exception e) {
                return null;
            }
        } else {
            long phone = Long.parseLong(phoneNumber);
            pn = new Phonenumber.PhoneNumber();
            pn.setCountryCode(86);
            pn.setNationalNumber(phone);
            country = "中国";
            descriptionForNumber = geocoder.getDescriptionForNumber(pn, Locale.SIMPLIFIED_CHINESE);
        }

        return country + " " + descriptionForNumber;

    }

    public static String randomPhone() {
        Random r = new Random();
        StringBuffer str = new StringBuffer();
        str.append("1");
        str.append(r.nextInt(1000) + 1000);
        str.append(r.nextInt(1000) + 1000);
        str.append("00");

        return str.toString();
    }

    public static void main(String[] args) throws NumberParseException {
        Phonenumber.PhoneNumber phoneNumber = PhoneNumberUtil.getInstance().parse("+442083661177", "CN");
        String descriptionForNumber = geocoder.getDescriptionForNumber(phoneNumber, Locale.CHINA);
        String code = PhoneNumberUtil.getInstance().getRegionCodeForNumber(phoneNumber);
//        System.out.printf(Locale.SIMPLIFIED_CHINESE.getLanguage());
        Locale locale = new Locale("zh", code);
        System.out.println(locale.getDisplayCountry(Locale.SIMPLIFIED_CHINESE));

//        System.out.println(descriptionForNumber);
        descriptionForNumber = geocoder.getDescriptionForNumber(phoneNumber, locale);
        System.out.println(descriptionForNumber);
    }
}
