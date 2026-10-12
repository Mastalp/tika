package org.apache.tika.serialization;

import org.junit.runner.RunWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;

@RunWith(value = JUnitPlatform.class)
@SelectClasses(value = { ComponentConfig_hasCustomLoader_7_3_Test.class, ComponentConfig_hasDefault_6_3_Test.class, ComponentConfig_hasListWrapper_4_3_Test.class })
public class ComponentConfig_Suite {
}
