/*
 * Copyright (c) 2012-2016 Augumenta Ltd. All rights reserved.
 *
 * This source code file is furnished under a limited license and may be used or
 * copied only in accordance with the terms of the license. Except as permitted
 * by the license, no part of this source code file may be  reproduced, stored in
 * a retrieval system, or transmitted, in any form or by  any means, electronic,
 * mechanical, recording, or otherwise, without the prior written permission of
 * Augumenta.
 *
 * This source code file contains proprietary information that is protected by
 * copyright. Certain parts of proprietary information is patent protected. The
 * content herein is furnished for informational use only, is subject to change
 * without notice, and should not be construed as a commitment by Augumenta.
 * Augumenta assumes no responsibility or liability for any errors or
 * inaccuracies that may appear in the informational content contained herein.
 * This source code file has not been thoroughly tested under all conditions.
 * Augumenta, therefore, does not guarantee or imply its reliability,
 * serviceability, or function.
 *
 */

package com.example.bryan.odginformar.AR_interaction_utils;

import android.graphics.PointF;

/**
 * RelativeMargin is a helper class used by PoseLayout and PoseCursorLayout. It helps
 * translating between the camera coordinates and screen coordinates.
 */
public class RelativeMargin {
	float top = 0.0f;
	float bottom = 0.0f;
	float left = 0.0f;
	float right = 0.0f;

	float width = 1.0f;
	float height = 1.0f;

	public RelativeMargin() {
		setMargin(0);
	}

	public RelativeMargin(float margin) {
		setMargin(margin);
	}

	public RelativeMargin(float horizontal, float vertical) {
		setMargin(horizontal, vertical);
	}

	public RelativeMargin(float left, float top, float right, float bottom) {
		setMargin(left, top, right, bottom);
	}

	public void setMargin(float margin) {
		setMargin(margin, margin, margin, margin);
	}

	public void setMargin(float horizontal, float vertical) {
		setMargin(horizontal, vertical, horizontal, vertical);
	}

	public void setMargin(float left, float top, float right, float bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		this.width = 1 - (this.left + this.right);
		this.height = 1 - (this.top + this.bottom);
	}

	public PointF translate(PointF point) {
		return translate(point.x, point.y);
	}

	public PointF translate(float x, float y) {
		return new PointF(translateX(x), translateY(y));
	}

	public float translateX(float x) {
		return x * this.width + this.left;
	}

	public float translateY(float y) {
		return y * this.height + this.top;
	}

	@Override
	public String toString() {
		return "RelativeMargin{" +
				"top=" + top +
				", bottom=" + bottom +
				", left=" + left +
				", right=" + right +
				'}';
	}
}
