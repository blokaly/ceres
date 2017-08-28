package com.blokaly.ceres.common

import spock.lang.Specification

class PromiseTest extends Specification {

    def 'Promise test' () {

        when:
        def promise = FullFill.<Integer>waterfall({ ->
            return 1;
        }).<Integer>next({input ->
           return input + 1;
        }).<Integer>next({input ->
            return input + 1;
        });

        then:
        promise.await() == 3;

    }
}
