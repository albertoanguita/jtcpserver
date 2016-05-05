package jacz.commengine.channel.tests.test1;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 16/05/12
 * Time: 18:23
 * To change this template use File | Settings | File Templates.
 */
public class NotSerClass implements Serializable {

    private static class InnerNotSerClass {
        public String s = "hello";
    }

    public Integer i = 5;

    public InnerNotSerClass ss;

    @Override
    public String toString() {
        return i.toString();
    }
}
