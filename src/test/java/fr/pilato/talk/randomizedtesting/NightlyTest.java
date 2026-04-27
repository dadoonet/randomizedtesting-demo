package fr.pilato.talk.randomizedtesting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Tag("Nightly")
@Test
@Timeout(value = 5, unit = TimeUnit.MINUTES)
public @interface NightlyTest {
}