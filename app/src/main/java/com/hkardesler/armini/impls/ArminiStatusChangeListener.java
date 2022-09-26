/*
 * *
 *  * Created by Alper Kardesler on 10.06.2022 07:47
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.impls;

import com.hkardesler.armini.models.ArminiStatusEnum;

public interface ArminiStatusChangeListener {
    void OnArminiStatusChanged(ArminiStatusEnum status);
}