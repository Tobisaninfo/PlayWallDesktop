package de.tobias.playpad.server;

import java.lang.annotation.*;

/**
 * Created by tobias on 13.02.17.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Name {

	String value();

}
