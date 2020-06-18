package com.goodapps.alarm.utils;

/**
 * Created by Dmitry Bochkov on 18.06.2020.
 */
import static java.lang.Math.abs;
public class PluralMergeUtil {
    public static String choosePluralMerge(long number, String caseOne, String caseTwo, String caseFive) {
        /* Выбирает правильную форму существительного в зависимости от числа.
           Чтобы легко запомнить, в каком порядке указывать варианты, пользуйтесь мнемоническим правилом:
           один-два-пять - один гвоздь, два гвоздя, пять гвоздей.
           [url]http://pyobject.ru/blog/2006/09/02/pytils/[/url]

           in: число и слово в трёх падежах.
           out: строка (число + существительное в нужном падеже).
         */

        String str = Long.toString(number) + " ";
        number = abs(number);

        if (number % 10 == 1 && number % 100 != 11) {
            str += caseOne;
        } else if (number % 10 >= 2 && number % 10 <= 4 && (number % 100 < 10 || number % 100 >= 20)) {
            str += caseTwo;
        } else {
            str += caseFive;
        }

        return str;
    }
}