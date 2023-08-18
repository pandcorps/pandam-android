/*
Copyright (c) 2009-2022, Andrew M. Martin
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

import java.io.*;
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.io.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.*;

import android.content.pm.*;

public class AndroidPangine extends GlPangine {
	protected static AndroidPangine engine = null;
	protected static PanActivity context = null;
	private static PanClipboard clip = null;
	protected static int desktopWidth = 0;
	protected static int desktopHeight = 0;
	private static Set<String> cacheFiles = null;
	
	protected AndroidPangine() {
		super(new AndroidPanteraction());
		Pangine.engine = this;
		engine = this;
		audio = new AndroidPanaudio();
		touchYInverted = true;
		setColorArrayNumChannels(4);
	}
	
	@Override
    public final int getDesktopWidth() {
        return desktopWidth;
    }
    
    @Override
    public final int getDesktopHeight() {
    	return desktopHeight;
    }
    
    @Override
    protected final boolean getDefaultFullScreeen() {
    	return true;
    }

    @Override
    protected final void initDisplay() throws Exception {
	    if (!fullScreen) {
	        throw new Exception("AndroidPangine requires full-screen mode");
	    }
	}
	
    @Override
	protected void initInput() throws Exception {
	}
    
    @Override
	protected final void initScreen() {
    	context.init();
	}

    @Override
	protected void stepControl() throws Exception {
		/*if (isCloseRequested()) {
			exit();
		}*/
    	//stepTouch();
	}
    
    @Override
    public final boolean isTouchSupported() {
    	return true;
    }
    
    @Override
  	public final boolean isMultiTouchSupported() {
  	    return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);
  	}
    
    protected final static class CopyResult {
    	protected final String fileName;
    	protected final long size;
    	
    	private CopyResult(final String fileName, final long size) {
    		this.fileName = fileName;
    		this.size = size;
    	}
    	
    	protected final FileInputStream openInputStream() throws Exception {
    		//return context.openFileInput(fileName);
    		return new FileInputStream(fileName); // We use cache directory, not app's private directory
    	}
    }
    
    protected final static CopyResult copyResourceToFile(final String loc) throws Exception {
        InputStream in = null;
        try {
            in = Iotil.getResourceInputStream(loc);
            return copyStreamToFile(loc, in);
        } finally {
            Iotil.close(in);
        }
    }
    
    protected final static NamedOutputStream getCopyOutputStream(final String loc) throws Exception {
		String tmpFileName = loc.replace('/', '_');
		
		//out = context.openFileOutput(tmpFileName, Context.MODE_PRIVATE);
		String dir = context.getCacheDir().getAbsolutePath();
		if (!dir.endsWith("/")) {
			dir += "/";
		}
		tmpFileName = dir + tmpFileName;
		return new NamedOutputStream(new FileOutputStream(tmpFileName), tmpFileName);
    }
    
    protected final static CopyResult copyStreamToFile(final String loc, final InputStream in) throws Exception {
        NamedOutputStream out = null;
        try {
            out = getCopyOutputStream(loc);
            final String tmpFileName = out.getName();
    		final int len = 1024;
    		int ret;
    		final byte[] buf = new byte[len];
    		//info("Getting resource input stream");
    		long size = 0;
    		while ((ret = in.read(buf)) >= 0) {
    			out.write(buf, 0, ret);
    			size += ret;
    			//info("Piping, size " + size);
    		}
    		if (cacheFiles == null) {
    			cacheFiles = new HashSet<String>();
    		}
    		cacheFiles.add(tmpFileName);
    		return new CopyResult(tmpFileName, size);
    	} finally {
    		Iotil.close(out);
    	}
    }
    
    @Override
	protected void onDestroy() {
    	// There have been ConcurrentModificationExceptions here; so use copy
    	for (final String cacheFile : Coltil.copy(cacheFiles)) {
    		new File(cacheFile).delete();
    	}
	}
	
    @Override
	protected void update() {
	}
    
    private final PanClipboard getClip() {
    	if (clip == null) {
    		try {
    			Class.forName("android.content.ClipboardManager");
    			clip = new ContentClipboard();
    		} catch (final Throwable e) {
    			clip = new TextClipboard();
    		}
    	}
    	return clip;
    }
    
    @Override
    public final void getClipboard(final Handler<String> handler) {
    	context.runOnUiThread(new Runnable() {
			@Override public final void run() {
				handler.handle(getClip().getClipboard());
			}});
	}
	
	@Override
	public final void setClipboard(final String value) {
		context.runOnUiThread(new Runnable() {
			@Override public final void run() {
				getClip().setClipboard(value);
			}});
	}
	
	@Override
	public final void setTitle(final String title) {
	}
	
	@Override
	public final void setIcon(final String... locations) {
	}
	
	protected final void runDestroy() throws Exception {
		destroy();
	}
}
