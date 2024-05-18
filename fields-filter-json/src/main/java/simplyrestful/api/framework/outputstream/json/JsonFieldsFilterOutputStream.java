package simplyrestful.api.framework.outputstream.json;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import simplyrestful.api.framework.filters.JsonFieldsFilter;

/**
 * This output stream filters on specific fields in a JSON object or array.
 *
 * This output stream buffers the stream until an entire JSON object or array is detected.
 * The field filtering will then be applied to that entire JSON object or array, which is
 * then written to the underlying output stream.
 *
 * If the first character written is not the start token of a JSON object or array, this
 * output stream will do nothing and write the byte directly to the underlying output
 * stream without buffering it.
 *
 * Unlike the BufferedOutputStream, this output stream will not flush the buffer to the
 * underlying stream when it is filled. Instead, it will increase the buffer size until an
 * entire JSON object or array is written. Only then will the buffer be written to the
 * underlying stream and the buffer size will be reset.
 *
 */
public class JsonFieldsFilterOutputStream extends BufferedOutputStream {
	public static final char JSON_START_OBJECT_TOKEN = '{';
	public static final char JSON_END_OBJECT_TOKEN = '}';
	public static final char JSON_START_ARRAY_TOKEN = '[';
	public static final char JSON_END_ARRAY_TOKEN = ']';
	public static final int INITIAL_BUFFER_SIZE = 8192;

	private final List<String> fields;

	private boolean isJson = false;
	private boolean isJsonObjectNotArray = false;
	private long nestingLevel = 0;

	public JsonFieldsFilterOutputStream(OutputStream out, List<String> fields) {
		super(out, INITIAL_BUFFER_SIZE);
		this.fields = fields;
	}

	@Override
	public synchronized void write(int b) throws IOException {
		if(!isJson(asByteArray(b))) {
			out.write(b);
			return;
		}
		ensureBufferSize(1);
		super.write(b);

		updateNestingLevel(asByteArray(b), 1);
		if(nestingLevel == 0) {
			filterJson();
		}
	}

	@Override
	public synchronized void write(byte[] b, int off, int len) throws IOException {
		if(!isJson(b)) {
			out.write(b, off, len);
			return;
		}
		ensureBufferSize(len);
		super.write(b, off, len);

		updateNestingLevel(b, len);
		if(nestingLevel == 0) {
			filterJson();
		}
	}

	/**
	 * Verifies if the output stream contains a JSON document.
	 *
	 * This is verified by checking that the first character written to the output stream is
	 * the start token for a JSON object or array. After the first character is written and
	 * verified, the result is stored globally and returned directly.
	 *
	 * @param b contains the data to be written.
	 * @return true if the output stream contains a JSON document.
	 */
	private boolean isJson(byte[] b) {
		if(count == 0) {
			String written = new String(b, StandardCharsets.UTF_8);
			boolean isJsonObject = written.startsWith(String.valueOf(JSON_START_OBJECT_TOKEN));
			boolean isJsonArray = written.startsWith(String.valueOf(JSON_START_ARRAY_TOKEN));
			isJson = isJsonObject || isJsonArray;
			isJsonObjectNotArray = isJsonObject && !isJsonArray;
		}
		return isJson;
	}

	private byte[] asByteArray(int b) {
		return new byte[] { (byte) b };
	}

	private void ensureBufferSize(int amountToBeAdded) {
		int estimatedBufferSize = count + amountToBeAdded;
		if(estimatedBufferSize >= buf.length) {
			int newBufferSize = estimatedBufferSize + INITIAL_BUFFER_SIZE;
			buf = Arrays.copyOf(buf, newBufferSize);
		}
	}

	private void updateNestingLevel(byte[] b, int len) {
		String written = new String(b, 0, len, StandardCharsets.UTF_8);
		long amountOfNestingStarts = written.chars()
				.filter(character -> character == (isJsonObjectNotArray ? JSON_START_OBJECT_TOKEN : JSON_START_ARRAY_TOKEN))
				.count();
		long amountOfNestingEnds = written.chars()
				.filter(character -> character == (isJsonObjectNotArray ? JSON_END_OBJECT_TOKEN : JSON_END_ARRAY_TOKEN))
				.count();
		nestingLevel = nestingLevel + amountOfNestingStarts - amountOfNestingEnds;
		if (nestingLevel < 0) {
			throw new IllegalStateException("The filter could not read the JSON object or array correctly");
		}
	}

	private void filterJson() throws IOException {
		String bufferedJsonObjectOrArray = new String(buf, 0, count, StandardCharsets.UTF_8);
		String filteredJson = new JsonFieldsFilter().filterFieldsInJson(bufferedJsonObjectOrArray, fields);
		byte[] filteredJsonBytes = filteredJson.getBytes(StandardCharsets.UTF_8);
		out.write(filteredJsonBytes, 0, filteredJsonBytes.length);
		reset();
	}

	private void reset() {
		count = 0;
		buf = new byte[INITIAL_BUFFER_SIZE];
		isJson = false;
		isJsonObjectNotArray  = false;
		nestingLevel = 0;
	}
}
