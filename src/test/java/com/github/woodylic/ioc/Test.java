package com.github.woodylic.ioc;

import com.github.woodylic.ioc.core.JsonApplicationContext;
import com.github.woodylic.ioc.entity.Robot;

public class Test {
    public static void main(String[] args) throws Exception {
        JsonApplicationContext applicationContext = new JsonApplicationContext("application.json");
        applicationContext.init();
        Robot aiRobot = (Robot) applicationContext.getBean("robot");

        aiRobot.show();
    }
}
