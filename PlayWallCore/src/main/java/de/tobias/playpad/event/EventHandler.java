package de.tobias.playpad.event;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {

}
