package sample;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import javafx.scene.image.ImageView;
import java.util.ArrayList;


public class Controller {
    @FXML
    Label titleLabel, numCardsLabel, attributeCheckLabel, playerUpdateLabel, clockDisplay, powerUpUsesLabel, playerTurnLabel;
    @FXML
    Label playerNamesLabel, playerScoresLabel, playerLivesLabel, resultsLabel, winnerLabel, eliminatedPlayersLabel;
    @FXML
    TextField p1Name, p2Name, p3Name, p4Name;
    @FXML
    Button flipCardsButton, getPointsButton, addPlayerButton, playButton;
    @FXML
    GridPane gdpPlayGrid, attributeGridPane;
    @FXML
    AnchorPane gameOverPane, gameplayPane, playerScreenPane, p2NamePane, p3NamePane, p4NamePane;
    @FXML
    ProgressBar powerUpBar;//https://stackoverflow.com/questions/13357077/javafx-progressbar-how-to-change-bar-color - how to change the color of the bar
    Image blank = new Image("resources/back_card.jpeg");


    //display attributes
    //I just added a few more "cards" that just displays which attribute is matching across all the cards clicked
    Image animalMatchImage = new Image("resources/match_animal.jpeg");
    Image bgMatchImage = new Image("resources/match_background.jpeg");
    Image colorMatchImage = new Image("resources/match_color.jpeg");

    //these are the power-up images I made to also make things look nice
    @FXML
    ImageView instantMatchImage = new ImageView("resources/instantMatch.jpg");
    @FXML
    ImageView shuffleImage = new ImageView("resources/shuffle.jpg");
    @FXML
    ImageView addTimeImage = new ImageView("resources/addTime.jpg");

    ArrayList<Card> cardsOnDeck = new ArrayList<>();//this determines the number of cards left
    //this is also one of the things that determine when the game would end
    ArrayList<Card> cardsOnBoard = new ArrayList<>();//this is useful for shuffling the cards and getting matches

    ArrayList<Player> players = new ArrayList<>();//this is an arraylist of players that are still in the game
    ArrayList<Player> eliminatedPlayers = new ArrayList<>();//these are for the players that lost all their lives
    private Player currentPlayer;//this represents the whoever's turn is at the moment

    private Card[][] board = new Card[4][5];//this represents the cards on the board
    private ImageView[][] boardImage = new ImageView[4][5];//this one's the cards' images
    private ImageView[][] attributeImage = new ImageView[3][1];//this one is just an extra object array for determining which attribute image to display

    private int rowIndex, colIndex, turn;
    private boolean canClickCard = true;
    private long last = System.currentTimeMillis();
    private int startingTime;//this represents the how long each turn is for multiplayer
    //or for singleplayer how long each game is
    private boolean match = false;//this determines whether there's a match in the cards clicked

    private boolean[] checkMatch = new boolean[3];//this is useful to know which attribute matches within the cards

    @FXML
    private void displayPlayerScreen(){//this happens after clicking the DOPE label
        titleLabel.setVisible(false);
        playerScreenPane.setVisible(true);
        createCards();
        setUpGame();
        placeCards();
    }
    private int countClick = 0;//this is just for this function, it determines how many times the "add player" button was clicked
    private ArrayList<String> playerNames = new ArrayList<>();
    @FXML//this is what happens when the "add player" button is clicked
    private void showMorePlayers(){
        countClick++;
        if(countClick==1){
            p2NamePane.setVisible(true);
        }
        else if(countClick==2){
            p3NamePane.setVisible(true);
        }
        else if(countClick==3){
            p4NamePane.setVisible(true);
            addPlayerButton.setVisible(false);//there's a limit of 4 players which is why you can't add players anymore
        }
    }

    private void setUpPlayers(){
        playerNames.add(p1Name.getText());
        playerNames.add(p2Name.getText());
        playerNames.add(p3Name.getText());
        playerNames.add(p4Name.getText());

        for(int i = 0; i<playerNames.size(); i++){
            if(!playerNames.get(i).equals("")){//this is just to "dummy proof" adding the players
                // in case they clicked the add player but don't want the extra player so they have no name,
                //this just makes sure that the only players playing are the ones with a name
                players.add(new Player(playerNames.get(i), 3,2,0));
            }
            else{
                playerNames.remove(i);
            }
        }
        turn = myRand(0,players.size()-1);//this chooses a random player depending on the num of players
        currentPlayer = players.get(turn);//it doesn't matter if it's single player because it will just be from 0,0
        if(players.size()==1){
            singlePlayerSetUp();
        }
        else{//each player in multiplayer has 20 seconds in their turn
            startingTime = 20;
        }
    }
    private void singlePlayerSetUp(){//this happens if there's only one name typed in the player screen
        startingTime = 90;//there's 1:30 if it's single player
        //if it's multiplayer then it's until all cards run out
        currentPlayer.setLives(5);//there's more lives since it's only single player
        currentPlayer.setPowerUpUse(3);
    }
    private void showAndUpdateLabels(){//this updates the labels in case there were changes to the values they represent
        playerScoresLabel.setText("");
        playerNamesLabel.setText("");
        for(int i = 0; i<players.size(); i++){
            playerScoresLabel.setText(playerScoresLabel.getText()+players.get(i).getPlayerPoints()+"\n");
        }

        for(int i = 0; i<players.size(); i++){
            playerNamesLabel.setText(playerNamesLabel.getText() + players.get(i).getName() + "\n" );
        }
        powerUpUsesLabel.setText(currentPlayer.getPowerUpUses()+"");
        playerTurnLabel.setText(currentPlayer.getName()+"'s turn");
        playerLivesLabel.setText(currentPlayer.getLives()+"");
    }

    @FXML
    private void showGameScreen(){//this happens when the start button at the bottom of player screen is clicked
        playerScreenPane.setVisible(false);
        gameplayPane.setVisible(true);
        setUpPlayers();
        showAndUpdateLabels();
        canClickCard = false;
    }

    @FXML
    private void startGame() {//this happens when the actual game screen is shown and the user clicked the play button above
        playButton.setVisible(false);
        gdpPlayGrid.setVisible(true);
        updateNumCards();
        powerUpBar.setProgress(1);
        startTimer();
        shuffleImage.setVisible(true);
        addTimeImage.setVisible(true);
        instantMatchImage.setVisible(true);
        canClickCard = true;
    }
    private void createCards() {//this creates the cards and adds them into the card arraylist
        //change the names of the images and make them the small caps and create new images in the loop instead of calling the image name
        String[] colors = {"B", "G", "R", "Y"};//this represents the letter for each color
        String[] background = {"C", "D", "H", "S"};//this one's the same but for the bg
        String[] animal = {"B", "C", "D", "P"};//and this one's the one for the animal

        for(int x = 0; x<4; x++){//this loop is for the "first" attribute which is color,
            //since there are four different kinds of each attribute, the each loop runs 4 times
            //having a total of 64 cards (4*4*4)
            for(int y = 0; y<4; y++){//this one's for bg
                for(int z = 0; z<4;z++){//this one's for the animal
                    //the colors[x] is just like substring but I created string arrays instead
                    // because I felt more comfortable coding it this way and it's not a lot of .substrings in the code
                    cardsOnDeck.add(new Card(colors[x] + background[y] + animal[z], new Image("resources/" + colors[x] + background[y] + animal[z] + ".jpeg")));
                }
            }
        }
    }
    private void setUpGame() {
        setUpCards();
        setUpAttributeCards();

        EventHandler z = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if(canClickCard){//this boolean changes when the cards don't match, the player can't click on a different card,
                    //the have to flip the cards to avoid cheating
                    rowIndex = GridPane.getRowIndex(((javafx.scene.image.ImageView) t.getSource()));
                    colIndex = GridPane.getColumnIndex(((javafx.scene.image.ImageView) t.getSource()));
                    displayCard(rowIndex, colIndex);
                }
                else{
                    playerUpdateLabel.setText("Flip the cards!");
                }

            }
        };
        for (int i = 0; i < boardImage.length; i++) {
            for (int j = 0; j < boardImage[i].length; j++) {
                //setting the onMouseClicked property for each of the ImageViews to call z (the event handler)
                boardImage[i][j].setOnMouseClicked(z);
            }
        }

    }
    private void setUpCards() {
        for (int i = 0; i < boardImage.length; i++) {//this makes all the cards be blank
            for (int j = 0; j < boardImage[i].length; j++) {
                boardImage[i][j] = new javafx.scene.image.ImageView();//this makes sure that each object within the array is an imageview
                boardImage[i][j].setImage(blank);//this makes all the cards flipped
                boardImage[i][j].setFitHeight(154);
                boardImage[i][j].setFitWidth(125);
                gdpPlayGrid.add(boardImage[i][j], j, i);//this adds the card in the grid
            }
        }
    }

    private void setUpAttributeCards() {
        for (int i = 0; i < attributeImage.length; i++) {//this sets up the attribute grid pane
            for (int j = 0; j < attributeImage[i].length; j++) {
                attributeImage[i][j] = new javafx.scene.image.ImageView();
                attributeImage[i][j].setFitHeight(129.6);
                attributeImage[i][j].setFitWidth(100);
                attributeImage[i][j].setImage(blank);
                attributeGridPane.add(attributeImage[i][j], j, i);
            }
        }
    }

    private int myRand(int lower, int upper){//this gets a random number based on the range from the parameters
        return (int)(Math.random()*(upper-lower+1))+ lower;
    }

    private void placeCards(){//this sets up the board by removing random cards from the deck and adding it to the cardsOnBoard
        //arraylist
        for(int row = 0; row<board.length; row++){
            for(int col = 0; col<board[row].length;col++){
                int randCard = myRand(0,cardsOnDeck.size()-1);//this gets a random card from the deck
                board[row][col] = cardsOnDeck.get(randCard);//this puts that card to the board
                cardsOnBoard.add(cardsOnDeck.get(randCard));//this puts that cards on the cardsOnBoard array
                cardsOnDeck.remove(cardsOnDeck.get(randCard));//this removes that card from the deck
            }
        }

    }
//    private long last = System.currentTimeMillis();
    private AnimationTimer timer;//I had to make an instance field so that I can stop the timer outside of the timer methods
    private void startTimer(){
        //I first saw this: https://docs.oracle.com/javase/8/javafx/api/javafx/animation/AnimationTimer.html
        //then I tried out some stuff just to know what how the methods are used
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                    //https://www.youtube.com/watch?v=CYUjjnoXdrM&t=597s --> this kind of gave me the idea for the if part
                    //it was around the 6:50 part, I didn't really watch the rest and skipped some parts
                    //the System.currentTimeMillis() was just something I found when I was typing
                    //and I used this to know more on how to use it:
                    // https://www.tutorialspoint.com/java/lang/system_currenttimemillis.htm
                    now = System.currentTimeMillis();// the actual date in miliseconds
                    if(now-last > 1000){//1000 milliseconds is one second,
                        // I tested it a few times and it can't exactly be 1000 milliseconds
                        // if you account for the time for Java takes to process the code
                        //so it has to be at least 1000 seconds for it to count as 1 second
                        last = now;
                        startingTime--;
                    }
                    if(startingTime==0){
                        if(players.size()>1){
                            switchTurn();//this switches the turn to the next player
                            start();//the timer resets
                        }
                        else{//this is for single-player mode since it doesn't have turns and it doesn't reset back
                            //the time is also set to 1:30 for one player
                            gameOverScreen();
                            stop();
                            System.out.println();
                        }
                    }
                    if(cardsOnDeck.size()<2){//if there are no more cards, then the game is over
                        gameOverScreen();
                        stop();
                    }
                    updateTimer();
                }
        };
        timer.start();
    }
    private void updateTimer(){
        //the stuff that I searched up (besides the timer) are just for display purposes only
        String seconds = String.format("%02d", getSeconds());//I searched up how to add leading zeroes
        //https://www.tutorialspoint.com/java-program-to-add-leading-zeros-to-a-number
        clockDisplay.setText(getMinutes(startingTime) + ":" + seconds);
    }
    private int getMinutes(int seconds){
        int minutes;
        minutes = seconds/60;//there are 60 seconds in one minute
        return minutes;
    }
    private int getSeconds(){
        int seconds = startingTime%60;//if the startingTime was 88 then that would mean there's 28 seconds left since that's the remainder of 88/60
        return seconds;
    }

    private void stopTimer(){
        timer.stop();
    }

    private ArrayList<Card> clickedCards = new ArrayList<>();//contains the clicked cards
    //this is used to know if there are any similarities within the cards clicked and also to flip them back

    private void displayCard(int r, int c){//this displays the card clicked (it like "flips" them but it just shows the picture of it)
        Card clickedCard = board[r][c];
        clickedCard.setRow(r);
        clickedCard.setColumn(c);
        boardImage[r][c].setImage(clickedCard.getCardImage());
        clickedCards.add(clickedCard);
        if(clickedCards.size()>1){//if there's more than one card clicked then it'll start to check if there are any similarities
            checkCards();
        }
    }
    //NOTES FROM CLASS: returning terminates the function (even if it's void)

    private int noMatches = 0;//this is for removing lives, not much for checking the cards
    private void checkCards() {
        match = false;//it assumes that there's no matches

        if(checkDoubleClick()) { //this checks if the player double clicked on a card,
            //this causes issues because everytime a player clicks on a card, it's a added to a separate array
            //that compares those clicked cards
            playerUpdateLabel.setText("");
            //testing while loop
            for (int i = 0; i < checkMatch.length; i++) {//this assumes that there are no matches yet,
                //and makes sure to reset the matches when a player clicks on a different card
                checkMatch[i] = false;
            }
            int checkAttribute = 0;
            while (checkAttribute < 3) {//I used a while instead of a for loop because I wanted to know where the checkAttribute was increasing
                //it was like this when I first tested it out so I decided to just keep it instead of making a for loop

                for (int cardPosition = 0; cardPosition < clickedCards.size() - 1; cardPosition++) { //you have to compare to all of the cards clicked
                    //if there's not a single match (checkMatch array is all false/didn't change), then the cards in that clickedCards arraylist would just have their image back to the
                    //blank image, then they would be removed from the clickedCards arraylist --> this is flipping the cards

                    if (checkIfCardsMatch(cardPosition, checkAttribute)) {//
                        checkMatch[checkAttribute] = true;
                    } else {//if two cards have diff attribute, then there's no use of checking the rest
                        // it should check if the next attribute is what all the cards have in common which is why there's a break
                        checkMatch[checkAttribute] = false;
                        break;
                    }
                }
                checkAttribute++;//this moves on to the next attribute and checks each card clicked if it's similar to the next attribute
            }
            if (checkMatch[0] == checkMatch[1] && checkMatch[1] == checkMatch[2]) {//this can't happen if there's a match
                //because all cards are unique and two cards can't have ALL the same attributes unless it's the same card
                //this checks if there is no match at all (all false in the checkMatch array)
                playerUpdateLabel.setText("No match");
                canClickCard = false;
                noMatches++;
                flipCardsButton.setVisible(true);
                getPointsButton.setVisible(false);
            } else {
                match = true;
                whichAttribute();
                getPointsButton.setVisible(true);
            }
        }
    }
    private boolean checkIfCardsMatch(int cardPosition, int checkAttribute){
        //there are 4 characteristics of the each of the 3 attributes: color(blue, red, green, yellow), background(s,h,d,c), animal(b,d,c,p)
        //so it compares the attribute at attribute[0] (that could be B, G, R or Y since it's comparing the color of the card)
        // of one card to the attribute[0] of the other card.if it doesn't match then it moves on to the next attribute
        // if there's a match the boolean array registers true in a specific position (0 - color, 1 - bg, 2 - animal) which means that
        //the cards have either the same color, bg or animal
        return clickedCards.get(cardPosition).getAttributes()[checkAttribute].equals(clickedCards.get(cardPosition+1).getAttributes()[checkAttribute]);
    }
    private void whichAttribute(){
        for(int i = 0; i<checkMatch.length; i++){
            if(checkMatch[i]){//if it's true then the attribute image would appear
                if(i==0){
                    attributeImage[0][0].setImage(colorMatchImage);
                }
                else if(i==1){
                    attributeImage[1][0].setImage(bgMatchImage);
                }
                else{
                    attributeImage[2][0].setImage(animalMatchImage);
                }
            }
            else{//if it's not then it would just be blank,
                // this is important in case the player initially matched cards with two same attributes
                //but clicked another card that only shares one same attribute across the rest
                attributeImage[i][0].setImage(blank);
            }
        }
    }
    private void clearAttributeGrid(){//this makes the attribute images flipped back to the blank display
        for(int i = 0; i<3; i++){
            attributeImage[i][0].setImage(blank);
        }
    }

    private boolean checkDoubleClick(){
        for(int i = 0; i<clickedCards.size()-1;i++){
            if(clickedCards.get(i).equals(clickedCards.get(i+1))){// this is to make sure the player didn't click on the same card
                playerUpdateLabel.setText("You already clicked \n       that card");
                clickedCards.remove(i);
                return false;
            }
        }
        return true;
    }

    @FXML
    private void flipCards(){//this makes sure that when there's no match, the player can flip the cards back
        clearAttributeGrid();
        if(noMatches>1) {//if the player incorrectly matches cards twice in a row, then they lose a life
            loseLife();
        }
        showAndUpdateLabels();
        playerUpdateLabel.setText("");
        canClickCard = true;//once the player flipped the cards, they can click on a card
        for(int cardPosition = 0; cardPosition<clickedCards.size();cardPosition++){//this "flips" all the cards in the clickedCards arraylist
            //by just setting their images to blank
            boardImage[clickedCards.get(cardPosition).getRow()][clickedCards.get(cardPosition).getColumn()].setImage(blank);
        }
        clickedCards.clear();//this shows that the player didn't click on any cards since they flipped them
        flipCardsButton.setVisible(false);
    }

    private void loseLife(){;
        noMatches = 0;//this makes sure that the player doesn't continuously lose lives if they incorrectly match cards again
        //they only lose lives if they incorrectly match only twice in a row and not more than that
        currentPlayer.setLives(currentPlayer.getLives()-1);
        if(currentPlayer.getLives()==0){
            eliminatePlayer();
        }
    }

    @FXML
    private void collectPoints(){
        noMatches = 0;//when you get a match then that means the player didn't incorrectly match twice in a row
        clearAttributeGrid();
        getPointsButton.setVisible(false);
        currentPlayer.setPlayerPoints(currentPlayer.getPlayerPoints()+(int)Math.pow(clickedCards.size(),2));
        showAndUpdateLabels();
        collectCards();// this collects the cards, I just wanted to separate them but I could also just put them in one function
    }

    private void collectCards(){
        for(int cardPosition = 0; cardPosition<clickedCards.size();cardPosition++){
            boardImage[clickedCards.get(cardPosition).getRow()][clickedCards.get(cardPosition).getColumn()].setImage(blank);
            cardsOnBoard.remove(clickedCards.get(cardPosition));//this removes the card in the deck and puts it onto the player's pile

            int randCard = myRand(0,cardsOnDeck.size()-1);//this one gets a new card from the deck and puts it on the board
            board[clickedCards.get(cardPosition).getRow()][clickedCards.get(cardPosition).getColumn()] = cardsOnDeck.get(randCard);
            cardsOnBoard.add(cardsOnDeck.get(randCard));
            cardsOnDeck.remove(cardsOnDeck.get(randCard));
        }
        clickedCards.clear();
        updateNumCards();
        if(cardsOnDeck.size()<2){
            gameOverScreen();
        }
    }

    private void updateNumCards(){
        numCardsLabel.setText(cardsOnDeck.size()+"");
    }

    @FXML
    private void addTime(){
        if(usePowerUp()) {
            startingTime += 8;//this adds 8 seconds to the timer
            updateTimer();
        }
        else{
            playerUpdateLabel.setText("Out of power-ups!");
        }
    }

    @FXML
    private void shuffleCards(){
        if(usePowerUp()) {
            flipCards();//this clears the clickedCards arraylist in case the player clicked the card
            //and also shuffling cards requires them all to be "flipped"
            int cardPosition = 0;
            for (int i = 0; i < cardsOnBoard.size(); i++) {//this loop shuffles the cards within the arraylist (cardsOnBoard) only
                //it doesn't show that it's been shuffled yet
                int randPosition = myRand(0, cardsOnBoard.size() - 1);//this gets a random card from the board
                cardsOnBoard.add(i, cardsOnBoard.remove(randPosition));//this shuffles the cards in the arraylist by putting that random card
                //like if it's in a deck it puts it on top, what ends up happening is there is a new order of cards in the arraylist
                // because of randomly picking cards from the board and rearranging them
            }
            //this displays the new shuffled cards of the cardsOnBoard arraylist
            for (int row = 0; row < board.length; row++) {
                for (int col = 0; col < board[row].length; col++) {
                    board[row][col] = cardsOnBoard.get(cardPosition);
                    cardPosition++;
                }
            }
        }
        else{
            playerUpdateLabel.setText("Out of power-ups!");
        }
    }

    @FXML
    private void getTwoMatches() {
        if(usePowerUp()) {
            match = false;
            for (int i = 0; i < cardsOnBoard.size(); i++) {
                for (int x = 0; x < 2; x++) {//this simulates getting two random cards
                    int randRow = myRand(0, 3);//getting a random card can be done in different ways
                    //but I just thought of getting a random row/col in the board and getting those two values to be passed onto
                    //the "clicking" and checking function
                    int randCol = myRand(0, 4);
                    displayCard(randRow, randCol);//this function thinks that the card has been "clicked"
                    //                    //so it puts it in the clicked cards arraylist, the function that the displayCard calls
                    // also checks the cards if there's more than 1 card clicked
                }
                if (match) {//this boolean is global and changes in the checkCard function
                    break;
                } else {
                    noMatches = 0;
                    flipCards();//if it's not matching, it "flips" it back, clearing the arraylist and repeating this loop all over again
                    //this happens really fast so the seeing the cards flipping over and over cannot be seen
                }
            }
        }
        else{
            playerUpdateLabel.setText("Out of power-ups!");
        }
    }

    private boolean usePowerUp(){//this checks if the current player has enough uses for a power up
        if(currentPlayer.getPowerUpUses()>0) {
            resetCoolDown();
            return true;
        }
        else{
            return false;
        }
    }

    private void resetCoolDown(){//this makes sure that the player doesn't either accidentally double click a power-up and run of out uses
        //or to make sure that the player doesn't abuse using the power-up
        powerUpBar.setVisible(true);
        shuffleImage.setVisible(false);
        addTimeImage.setVisible(false);
        instantMatchImage.setVisible(false);

        currentPlayer.setPowerUpUse(currentPlayer.getPowerUpUses() - 1);//this decreases the currentPlayer's uses for power-ups
        showAndUpdateLabels();

        powerUpBar.setProgress(0);// I used this to at least get the gist of how it works:
        //http://tutorials.jenkov.com/javafx/progressbar.html#:~:text=The%20JavaFX%20ProgressBar%20is%20a,scene.control.ProgressBar%20class.
        AnimationTimer powerBarTimer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                powerUpBar.setProgress(powerUpBar.getProgress() + 0.005);
                if (powerUpBar.getProgress() >= 1) {
                    stop();
                    powerUpBar.setVisible(false);//the animation that ends up happening is almost like a loading screen
                    //where when it hits the end, the bar disappears and the power-ups appear
                    shuffleImage.setVisible(true);
                    addTimeImage.setVisible(true);
                    instantMatchImage.setVisible(true);
                }
            }
        };
        powerBarTimer.start();
    }


    private void switchTurn(){//this switches the turn if it's multiplayer
        noMatches = 0;//this makes sure that a player loses lives within their turn
        startingTime = 20;
        flipCards();
        turn++;
        currentPlayer = players.get(turn%players.size());//turn%players.size() would always be in the range of num of players
        //so this gets who ever the next player is since it's turn++
        showAndUpdateLabels();//this updates whoever's turn it is in the label and their corresponding num of lives or power-up uses
        getPointsButton.setVisible(false);//this makes sure that the next player doesn't get points when there are no cards flipped
    }

    private void eliminatePlayer(){
        if(players.size()>1){//it only adds a player into the eliminatedPlayers arraylist if it's multiplayer
            players.remove(currentPlayer);
            eliminatedPlayers.add(currentPlayer);
            switchTurn();
            showAndUpdateLabels();
        }
        if(players.size()<=1){//if there's only one player left or less then that means the game ends
            //or if it's single-player and the player lost all their lives, the game ends
            gameOverScreen();
        }
    }

    private void gameOverScreen(){
        stopTimer();
        gameplayPane.setVisible(false);
        gameOverPane.setVisible(true);
        if(players.size()>1 || eliminatedPlayers.size()>0){//this checks if there are either more than one player left
            //or if it's two players then it checks if there is an eliminated player
            int max = 0;//this represents the highest score among the players
            String winner = "";
            for(int i = 0; i<players.size(); i++){
                if(players.get(i).getPlayerPoints()>max){//this checks if there is a higher score than the one before
                    max = players.get(i).getPlayerPoints();
                    winner = players.get(i).getName();
                }
            }
            winnerLabel.setText("The winner is " + winner + "!");//whoever is the last player that satisfied the if is the winner
            if(eliminatedPlayers.size()>0){//this is for displaying if there are any eliminated players
                eliminatedPlayersLabel.setText("Eliminated Players:\n");
            }
            for(int i = 0; i<eliminatedPlayers.size();i++){
                eliminatedPlayersLabel.setText(eliminatedPlayersLabel.getText() + eliminatedPlayers.get(i).getName() + ": " + eliminatedPlayers.get(i).getPlayerPoints() + " points\n");
            }
        }
        else{
            winnerLabel.setText("Results:");
        }
        for(int i = 0; i<players.size(); i++){//this displays the scores of the people who didn't get eliminated/the score of the player
            //if the game is single-player
            resultsLabel.setText(resultsLabel.getText() + players.get(i).getName() + ": " + players.get(i).getPlayerPoints() + " points\n" );
        }
    }
    @FXML
    private void playAgain(){//this resets everything that includes the labels and arraylist that need to be cleared, stuff that should/nt ne visible, etc
        gameOverPane.setVisible(false);
        titleLabel.setVisible(true);
        p2NamePane.setVisible(false);
        p3NamePane.setVisible(false);
        p4NamePane.setVisible(false);
        playButton.setVisible(true);
        addPlayerButton.setVisible(true);

        resultsLabel.setText("");
        eliminatedPlayersLabel.setText("");
        p1Name.setText("");
        p2Name.setText("");
        p3Name.setText("");
        p4Name.setText("");
        numCardsLabel.setText("");
        countClick = 0;
        cardsOnDeck.clear();
        cardsOnBoard.clear();
        clickedCards.clear();
        players.clear();
        eliminatedPlayers.clear();
        playerNames.clear();

    }

}



    

