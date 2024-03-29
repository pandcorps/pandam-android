/*
Copyright (c) 2009-2023, Andrew M. Martin
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
   disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of Pandam nor the names of its contributors may be used to endorse or promote products derived from this
   software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/
package org.pandcorps.pandam.android;

import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.*;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.*;

import android.opengl.GLSurfaceView.*;

public final class PanRenderer implements Renderer {
	private static volatile Pangame game = null;
	
	protected PanRenderer() {
	}

	@Override
	public final void onDrawFrame(final GL10 gl) {
		try {
			if (!AndroidPangine.engine.isRunning()) {
				AndroidPangine.engine.runDestroy();
				System.exit(0);
				return;
			}
			AndroidPangine.engine.frame();
		} catch (final Exception e) {
			//e.printStackTrace();
			throw Pantil.toRuntimeException(e);
		}
	}

	@Override
	public final void onSurfaceChanged(final GL10 gl, final int width, int height) {
	    // Sets the actual width/height reported by the device (game can still set up camera to achieve low resolution)
	    AndroidPangine.desktopWidth = width;
        AndroidPangine.desktopHeight = height;
        AndroidPangine.engine.forceDisplaySize(width, height);
        // Sets the game's desired width/height (used with setFixedSize in onSurfaceCreated below achieves low resolution)
        /*
	    AndroidPangine.desktopWidth = AndroidPangine.engine.getEffectiveWidth();
        AndroidPangine.desktopHeight = AndroidPangine.engine.getEffectiveHeight();
        AndroidPangine.engine.forceDisplaySize(AndroidPangine.engine.getEffectiveWidth(), AndroidPangine.engine.getEffectiveHeight());
        */
	}

	@Override
	public final void onSurfaceCreated(final GL10 gl, final EGLConfig config) {
		/*
		Also called when surface is recreated.
		(After waking up from sleep.)
		Need to reload textures after 1st time.
		*/
		AndroidPangine.gl = new AndroidPangl(gl);
		
		try {
			if (game == null) {
				GlPanmage.saveTextures = true;
				game = PanActivity.activity.newGame();
				game.beforeLoop();
			} else {
				game.recreate();
			}
		} catch (final Exception e) {
			//e.printStackTrace();
			throw Pantil.toRuntimeException(e);
		}
	}
}
