package com.crv.myapplication.assist;

import com.asha.vrlib.strategy.projection.AbsProjectionStrategy;
import com.asha.vrlib.strategy.projection.IMDProjectionFactory;
import com.asha.vrlib.strategy.projection.MultiFishEyeProjection;

/**
 * Created by NGN_PRINT on 2016-09-19.
 */

public class CustomProjectionFactory implements IMDProjectionFactory {

    public static final int CUSTOM_PROJECTION_FISH_EYE_RADIUS_VERTICAL = 9611;

    @Override
    public AbsProjectionStrategy createStrategy(int mode) {
        switch (mode){
            case CUSTOM_PROJECTION_FISH_EYE_RADIUS_VERTICAL:
                return new MultiFishEyeProjection(0.745f,false);
            default:return null;
        }
    }
}
