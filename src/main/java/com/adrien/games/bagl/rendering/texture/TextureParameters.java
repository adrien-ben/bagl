package com.adrien.games.bagl.rendering.texture;

/**
 * <p>Parameters for textures. The different parameters are :
 * <ul>
 * <li>format : The format of the texture. Default is RGBA8.
 * <li>minFilter : The minification filter of the texture. Default is LINEAR.
 * <li>magFilter : The magnification filter of the texture. Default is LINEAR.
 * <li>sWrap : The wrapping of the texture for the u component. Default is REPEAT.
 * <li>tWrap : The wrapping of the texture for the v component. Default is REPEAT.
 * <li>anisotropic : The level of anisotropic filtering (should be 0, 2, 4, 8 or 16). Default is 0;
 * <li>mipmaps : Flag indicating if mimaps must be generated. Default is false.
 * <br><br>
 * <p>Texture parameters objects can be created in a fluent way. ex: new TextureParameters().anisotropic(16).mipmaps(true);
 * 
 * @see Format
 * @see Filter
 * @see Wrap
 * 
 * @author Adrien
 *
 */
public class TextureParameters {

	private Format format = Format.RGBA8;
	private Filter minFilter = Filter.LINEAR;
	private Filter magFilter = Filter.LINEAR;
	private Wrap sWrap = Wrap.REPEAT;
	private Wrap tWrap = Wrap.REPEAT;
	private int anisotropic = 0;
	private boolean mipmaps = false;
	
	public TextureParameters format(Format format) {
		this.format = format;
		return this;
	}
	
	public TextureParameters minFilter(Filter filter) {
		this.minFilter = filter;
		return this;
	}
	
	public TextureParameters magFilter(Filter filter) {
		this.magFilter = filter;
		return this;
	}
	
	public TextureParameters sWrap(Wrap wrap) {
		this.sWrap = wrap;
		return this;
	}
	
	public TextureParameters tWrap(Wrap wrap) {
		this.tWrap = wrap;
		return this;
	}
	
	public TextureParameters anisotropic(int anisotropic) {
		this.anisotropic = anisotropic;
		return this;
	}
	
	public TextureParameters mipmaps(boolean generate) {
		this.mipmaps = generate;
		return this;
	}

	public Format getFormat() {
		return format;
	}

	public Filter getMinFilter() {
		return minFilter;
	}

	public Filter getMagFilter() {
		return magFilter;
	}

	public Wrap getsWrap() {
		return sWrap;
	}

	public Wrap gettWrap() {
		return tWrap;
	}

	public int getAnisotropic() {
		return anisotropic;
	}
	
	public boolean getMipmaps() {
		return mipmaps;
	}
	
}
