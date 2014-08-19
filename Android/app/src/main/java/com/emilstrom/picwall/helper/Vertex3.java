package com.emilstrom.picwall.helper;

/**
 * Created by Emil on 2014-02-20.
 */
public class Vertex3 {
	public float x, y, z;
	public Vertex3() {
		x = 0;
		y = 0;
		z = 0;
	}
	public Vertex3(float xx, float yy, float zz) {
		x = xx;
		y = yy;
		z = zz;
	}
	public Vertex3(Vertex3 v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}

	public void add(Vertex3 v) {
		x += v.x;
		y += v.y;
		z += v.z;
	}
	public void subtract(Vertex3 v) {
		x -= v.x;
		y -= v.y;
		z -= v.z;
	}
	public void multiply(Vertex3 v) {
		x *= v.x;
		y *= v.y;
		z *= v.z;
	}
	public void multiply(float d) {
		x *= d;
		y *= d;
		z *= d;
	}

	public Vertex3 plus(Vertex3 v) {
		return Vertex3.add(this, v);
	}
	public Vertex3 minus(Vertex3 v) {
		return Vertex3.subtract(this, v);
	}
	public Vertex3 times(Vertex3 v) {
		return Vertex3.multiply(this, v);
	}
	public Vertex3 times(float d) {
		return Vertex3.multiply(this, d);
	}

	public void normalize() {
		float l = getLength();
		x /= l;
		y /= l;
		z /= l;
	}
	public void reverse() { multiply(-1); }

	public void copy(Vertex3 v) { x = v.x; y = v.y; z = v.z; }

	public float getLength() { return getLength(this); }


	///
	public Vertex3 getNormalized() {
		Vertex3 v = new Vertex3(this);
		v.normalize();

		return v;
	}

	public boolean compare(Vertex3 v) { return (x == v.x && y == v.y); }

	public Vertex2 toVertex2() {
		return new Vertex2(x, y);
	}

	public static Vertex3 add(Vertex3 a, Vertex3 b) {
		return new Vertex3(a.x + b.x, a.y + b.y, a.z + b.z);
	}

	public static Vertex3 subtract(Vertex3 a, Vertex3 b) {
		return new Vertex3(a.x - b.x, a.y - b.y, a.z - b.z);
	}

	public static Vertex3 multiply(Vertex3 a, Vertex3 b) {
		return new Vertex3(a.x * b.x, a.x * b.y, a.z * b.z);
	}

	public static Vertex3 multiply(Vertex3 a, float d) {
		return new Vertex3(a.x * d, a.y * d, a.z * d);
	}

	public static Vertex3 normalize(Vertex3 v) {
		if (v.x == 0 && v.y == 0) return new Vertex3(0,0,0);

		float l = v.getLength();
		return new Vertex3(v.x / l, v.y / l, v.x / l);
	}

	public static Vertex3 getDirectionVertex(Vertex3 a, Vertex3 b) {
		return normalize(subtract(b, a));
	}

	public static float getLength(Vertex3 a, Vertex3 b) {
		Vertex3 v = subtract(a, b);
		if (v.x == 0f && v.y == 0f && v.z == 0) return 0f;

		return v.getLength();
	}

	public static float getLength(Vertex3 v) {
		return (float)Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
	}
}