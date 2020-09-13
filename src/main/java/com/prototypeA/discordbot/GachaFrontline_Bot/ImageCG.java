package com.prototypeA.discordbot.GachaFrontline_Bot;


public class ImageCG {

	private String name;
	private String url;


	public ImageCG(String name, String url) {
		this.name = name;
		this.url = url;
	}

	/**
	 * Returns the name of the CG
	 *
	 * @return The name of the CG
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the image url of the CG
	 *
	 * @return The url of the CG image
	 */
	public String getImageUrl() {
		return this.url;
	}
}
