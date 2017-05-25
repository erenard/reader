/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manga.worker;

/**
 *
 * @author ingenieur9
 */
public enum Orientation {
    VERTICAL(-2, 2),
    HORIZONTAL(88, 92);
    
    final int angleMin, angleMax;
    final double theta;

    private Orientation(int angleMin, int angleMax) {
        this.angleMin = angleMin;
        this.angleMax = angleMax;
        this.theta = Math.PI * ((angleMin + angleMax) / 2) / 180;
    }
    
    public Orientation toggle() {
        return this.equals(HORIZONTAL) ? VERTICAL : HORIZONTAL;
    }
}
