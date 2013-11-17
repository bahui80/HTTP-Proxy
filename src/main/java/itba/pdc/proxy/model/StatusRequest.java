package itba.pdc.proxy.model;

public enum StatusRequest {
	/*
	 * BYTES, ACCESS, HISTOGRAM, STATUS, TRANSFORMER
	 * 
	 * This status are for Ehttp
	 */
	OK(200), BAD_REQUEST(400), METHOD_NOT_ALLOWED(405), VERSION_NOT_SUPPORTED(
			505), CONFLICT(409), LENGTH_REQUIRED(411), BYTES(200), ACCESSES(200), HISTOGRAM(
			200), STATUS(200), MISSING_HOST(400), FILTER(400), CLOSED_CHANNEL(
			409), INVALID_HOST_PORT(503), UNAUTHORIZED(401);

	private final int sId;

	private StatusRequest(int id) {
		this.sId = id;
	}

	public int getId() {
		return sId;
	}
}
