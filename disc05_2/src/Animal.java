public class Animal {
    int speak(Dog a) {
        return 1;
    }

    int speak(Animal a) {
        return 2;
    }
}

class Dog extends Animal {
    int speak(Animal a) {
        return 3;
    }
}

class Poodle extends Dog {
    int speak(Dog a) {
        return 4;
    }
}
