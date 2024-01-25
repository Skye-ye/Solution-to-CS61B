public class Dog{
	public int weightInPounds;

	public Dog(int startingWeight){
		weightInPounds = startingWeight;
	}
	
	public void makeNoise(){
		if (weightInPounds < 10){
			System.out.println("yip!");
		} else if (weightInPounds < 30){
			System.out.println("Bark!");
		} else {
			System.out.println("Woooof!");
		}
	}
}