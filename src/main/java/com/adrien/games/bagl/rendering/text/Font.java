package com.adrien.games.bagl.rendering.text;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;

import com.adrien.games.bagl.rendering.texture.Format;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureParameters;
import com.adrien.games.bagl.rendering.texture.TextureRegion;
import com.adrien.games.bagl.utils.FileUtils;

public class Font {

	private static final int BMP_WIDTH = 512;
	private static final int BMP_HEIGHT = 512;
	private static final int FIRST_CHAR = 32;
	private static final int CHAR_COUNT = 96;
	
	private final int size;
	private Texture bitmap;
	private Char[] chars = new Char[CHAR_COUNT];
	private int ascent;
	private int descent;
	private int lineGap;
	
	public Font(String filePath, int size) {
		this.size = size;
		this.load(filePath);
	}
	
	private void load(String filePath) {
			ByteBuffer ttf = FileUtils.loadAsByteBuffer(filePath);
			
			STBTTBakedChar.Buffer cdata = STBTTBakedChar.malloc(CHAR_COUNT);
			ByteBuffer bitmap = BufferUtils.createByteBuffer(BMP_WIDTH * BMP_HEIGHT);
			STBTruetype.stbtt_BakeFontBitmap(ttf, this.size, bitmap, BMP_WIDTH, BMP_HEIGHT, FIRST_CHAR, cdata);
			this.bitmap = new Texture(BMP_WIDTH, BMP_HEIGHT, bitmap, new TextureParameters().format(Format.ALPHA8));
			
			STBTTFontinfo infos = STBTTFontinfo.malloc();
			STBTruetype.stbtt_InitFont(infos, ttf);

			this.getFontVMetrics(infos);
			this.getCharInfos(infos, cdata);
			
			infos.free();
			cdata.free();
	}
	
	private void getFontVMetrics(STBTTFontinfo infos) {
		float scale = STBTruetype.stbtt_ScaleForPixelHeight(infos, this.size);
		int[] ascent = new int[1];
		int[] descent = new int[1];
		int[] lineGap = new int[1];
		STBTruetype.stbtt_GetFontVMetrics(infos, ascent, descent, lineGap);
		this.ascent = (int)(scale*ascent[0]);
		this.descent = (int)(scale*descent[0]);
		this.lineGap = (int)(scale*lineGap[0]);
	}
	
	private void getCharInfos(STBTTFontinfo infos, STBTTBakedChar.Buffer cdata) {			
		int[] width = new int[1];
		int[] leftBearing = new int[1];
		for(int i = 0; i < CHAR_COUNT; i++) {
			int codePoint = i + FIRST_CHAR;
			STBTruetype.stbtt_GetCodepointHMetrics(infos, codePoint, width, leftBearing);
			chars[i] = createChar(cdata.get(i), (char)codePoint);
		}
	}
	
	private Char createChar(STBTTBakedChar charBuffer, char c) {
		float left = charBuffer.x0();
		float bottom = charBuffer.y0();
		float right = charBuffer.x1();
		float top = charBuffer.y1();
		TextureRegion region = new TextureRegion(this.bitmap, left/BMP_WIDTH, top/BMP_HEIGHT, 
				right/BMP_WIDTH, bottom/BMP_HEIGHT);
		return new Char(region, (int)(right - left), (int)(top - bottom), (int)charBuffer.xoff(), 
				(int)charBuffer.yoff(), charBuffer.xadvance(), c);
	}
	
	public Char getChar(char c) {
		int index = (int)c - FIRST_CHAR;
		return (index < 0 || index >= CHAR_COUNT) ?  null : this.chars[index];
	}
	
	public void destroy() {
		this.bitmap.destroy();
	}
	
	public int getSize() {
		return size;
	}

	public Texture getBitmap() {
		return bitmap;
	}

	public int getAscent() {
		return ascent;
	}

	public int getDescent() {
		return descent;
	}

	public int getLineGap() {
		return lineGap;
	}
	
}
