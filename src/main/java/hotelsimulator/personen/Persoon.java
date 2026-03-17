package hotelsimulator.personen;

import hotelsimulator.ruimtes.HotelRuimte;

public abstract class Persoon {
	protected HotelRuimte huidigeRuimte;

	public Persoon() {
		// standaard logica voor elke persoon
	}

	public HotelRuimte getHuidigeRuimte() {
		return huidigeRuimte;
	}

	public void setHuidigeRuimte(HotelRuimte huidigeRuimte) {
		this.huidigeRuimte = huidigeRuimte;
	}

	public void bewegenNaar(HotelRuimte doelRuimte) {
		this.huidigeRuimte = doelRuimte;
		// hier kan later de pathfinding komen
		// om de kortste route te berekenen.
	}
}