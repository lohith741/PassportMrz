package PassportMrz;

public class CharCoordinate {

	private String string = "";

	private char character;

	private double left, right, top, bottom, index, charConfidence, suspicious;

	public CharCoordinate() {
		super();
	}

	public CharCoordinate(double left, double right, double top, double bottom, String string) {
		super();
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
		this.string = string;
	}

	public CharCoordinate(String string, char character, double left, double right, double top, double bottom,
			double index, double charConfidence, double suspicious) {
		super();
		this.string = string;
		this.character = character;
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
		this.index = index;
		this.charConfidence = charConfidence;
		this.suspicious = suspicious;
	}

	@Override
	public String toString() {
		return "CharCoordinate [string=" + string + ", character=" + character + ", left=" + left + ", right=" + right
				+ ", top=" + top + ", bottom=" + bottom + ", index=" + index + ", charConfidence=" + charConfidence
				+ ", suspicious=" + suspicious + "]";
	}

	public String getString() {
		return string;
	}

	public char getCharacter() {
		return character;
	}

	public double getLeft() {
		return left;
	}

	public double getRight() {
		return right;
	}

	public double getTop() {
		return top;
	}

	public double getBottom() {
		return bottom;
	}

	public double getIndex() {
		return index;
	}

	public double getCharConfidence() {
		return charConfidence;
	}

	public double getSuspicious() {
		return suspicious;
	}

	public void setString(String string) {
		this.string = string;
	}

	public void setCharacter(char character) {
		this.character = character;
	}

	public void setLeft(double left) {
		this.left = left;
	}

	public void setRight(double right) {
		this.right = right;
	}

	public void setTop(double top) {
		this.top = top;
	}

	public void setBottom(double bottom) {
		this.bottom = bottom;
	}

	public void setIndex(double index) {
		this.index = index;
	}

	public void setCharConfidence(double charConfidence) {
		this.charConfidence = charConfidence;
	}

	public void setSuspicious(double suspicious) {
		this.suspicious = suspicious;
	}
}
