package com.zebrunner.agent.testng.core;

import com.zebrunner.agent.core.registrar.maintainer.MaintainerResolver;
import lombok.RequiredArgsConstructor;
import org.testng.xml.XmlSuite;

import java.lang.reflect.Method;

@RequiredArgsConstructor
public class RootXmlSuiteMaintainerResolver implements MaintainerResolver {

    private final XmlSuite rootXmlSuite;

    @Override
    public String resolve(Class<?> aClass, Method method) {
        return rootXmlSuite.getParameter("maintainer");
    }

}
