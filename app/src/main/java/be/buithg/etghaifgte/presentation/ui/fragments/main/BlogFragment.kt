package be.buithg.etghaifgte.presentation.ui.fragments.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import be.buithg.etghaifgte.R
import be.buithg.etghaifgte.databinding.FragmentBlogBinding
import androidx.navigation.fragment.findNavController


class BlogFragment : Fragment() {

    private lateinit var binding: FragmentBlogBinding

    private val items by lazy {
        listOf(
            BlogItem(
                title = "How to analyze team for smarter betting",
                summary = "Tips on assessing team performances",
                imageRes = R.drawable.blog_image_1,
                content = BLOG_TEXT_1
            ),
            BlogItem(
                title = "The importance of weather conditions in cricket betting",
                summary = "How weather affects your bets",
                imageRes = R.drawable.blog_image_2,
                content = BLOG_TEXT_2
            ),
            BlogItem(
                title = "Top Mistakes to avoid when betting on cricket",
                summary = "Avoid these pitfalls when betting",
                imageRes = R.drawable.blog_image_3,
                content = BLOG_TEXT_3
            ),
            BlogItem(
                title = "Maximizing your cricket betting bankroll",
                summary = "Tips on assessing team perfomances",
                imageRes = R.drawable.blog_image_4,
                content = BLOG_TEXT_4
            )
        )
    }

    companion object {
        private const val BLOG_TEXT_1 = """
How to Analyze Team Form for Smarter Betting 
When it comes to betting on cricket, one of the most crucial factors to consider is the team’s 
form. A team’s form refers to its recent performance in matches, including wins, losses, player 
performances, and how well they are playing as a unit. Analyzing team form helps bettors make 
smarter, more informed decisions when placing their bets. 
Key Aspects to Consider: 

1. Recent Results: 
Always check how a team has been performing in their most recent matches. A winning 
streak or a string of losses can reveal how well the team is currently playing. This 
includes not only the results but the quality of the opposition they faced. 

2. Player Form: 
Individual performances are a big part of team form. A key player in top form, such as a 
top-order batsman or a wicket-taking bowler, can significantly influence the outcome of a 
match. Pay attention to players who are performing consistently well and those who have 
been struggling. 

3. Home vs Away Form: 
Some teams perform much better at home, where they are familiar with the pitch and 
weather conditions. On the other hand, some teams are strong travelers and can win 
anywhere. It’s essential to analyze the team’s performance at home and away to get a 
complete picture. 

4. Head-to-Head Record: 
Look at how teams have performed against each other historically. A strong head-to-head 
record can give you insights into potential outcomes. Some teams have a psychological 
advantage over others, and knowing this can be an edge for your bet. 

5. Injuries and Squad Changes: 
Injuries to key players or last-minute changes in the squad can impact a team’s form. 
Always stay updated on the latest squad announcements before placing any bets. 
By thoroughly analyzing a team’s recent form, player performances, and other contextual 
factors, bettors can make more accurate predictions and increase their chances of success.
"""

        private const val BLOG_TEXT_2 = """
The Importance of Weather Conditions in Cricket Betting 
Weather conditions can have a significant impact on the outcome of a cricket match. Whether it's 
the pitch conditions, visibility, or wind speed, weather can alter the dynamics of the game in a 
way that bettors should always consider when placing their wagers. 

Key Weather Factors to Watch: 
1. Pitch Conditions: 
Weather affects the condition of the pitch, which in turn influences how the ball behaves. 
On a dry, hot day, the pitch might break up and favor spinners, while overcast conditions 
with humidity might assist swing bowlers. Understanding the pitch's likely behavior can 
help determine whether it's a good day for batsmen or bowlers. 

2. Rain Delays and Interruption: 
In cricket, rain delays can have a massive impact on a match's outcome. A match can be 
reduced to a shorter format, or even abandoned entirely, due to heavy rain. Bettors need 
to keep track of weather forecasts, as rain interruptions could change betting odds and 
strategies. 

3. Humidity and Dew: 
Humidity can influence how the ball behaves, particularly for swing bowlers. High 
humidity often aids bowlers who can move the ball in the air. Dew, especially in the 
evening, can make the outfield wet and affect batting, as the ball becomes harder to grip 
for bowlers, leading to higher scores. 

4. Wind and Temperature: 
The wind speed and direction can also influence a match, especially in stadiums near the 
coast. Wind can assist bowlers with swing and affect field placements, while extreme 
temperatures might tire players out more quickly, influencing their performance as the 
match progresses. 

5. Weather and Team Strategy: 
Teams might alter their strategies based on the weather. For example, a team batting first 
on a dry, hot day might aim for a bigger total, while one batting second in cool, humid 
conditions might have an advantage. Understanding the team’s approach to different 
weather conditions will give you a strategic edge when betting. 
By closely monitoring weather forecasts and understanding its impact on the game, bettors can 
enhance their chances of making successful bets. Weather is unpredictable, but with the right 
knowledge and analysis, it can be turned into a valuable tool for smarter betting decisions.
"""

        private const val BLOG_TEXT_3 = """
Top Mistakes to Avoid When Betting on Cricket 
Betting on cricket can be a thrilling experience, but it’s important to avoid common mistakes that 
can lead to poor decisions and losses. To maximize your chances of success, here are the top 
mistakes to avoid when betting on cricket. 

1. Not Doing Enough Research 
One of the biggest mistakes bettors make is placing bets without thorough research. Cricket is a 
complex sport with many variables, such as player form, team dynamics, pitch conditions, and 
weather. Always take the time to research the teams, their recent performances, the conditions of 
the match, and any other relevant factors before placing a bet. 

2. Ignoring the Impact of Weather 
Weather plays a huge role in cricket, affecting both the pitch conditions and the flow of the 
game. Bettors often overlook this factor, but understanding the forecast and its potential impact 
on the match can make a big difference. For instance, humid conditions might help swing 
bowlers, while rain delays might change a team’s strategy. 

3. Betting on Your Favorite Team 
It’s easy to get emotionally attached to your favorite team, but this bias can cloud your judgment. 
Always base your bets on logic, data, and analysis rather than personal feelings. Blindly betting 
on a team because you support them can lead to unnecessary losses. 

4. Chasing Losses 
Chasing losses is a dangerous mindset in any form of betting. When you lose a bet, it’s natural to 
want to make that money back, but chasing losses often leads to more losses. Stick to your 
strategy, set a budget, and avoid making rash bets in an attempt to recover from previous 
mistakes. 

5. Overestimating the Power of Favorites 
Favorites are often touted as the team most likely to win, but overestimating their strength can 
lead to poor bets. In cricket, upsets are frequent, and underdog teams can often surprise. Always 
consider the value in betting on the underdog, especially when the odds are stacked against them. 

6. Betting on Every Match 
Not every match offers a good betting opportunity. Bettors sometimes place bets on every game, 
which can lead to losses. Instead, be selective about the matches you bet on. Focus on those 
where you’ve done proper research and feel confident about the outcome. 

7. Betting Without a Plan or Bankroll Management 
A lack of proper bankroll management is one of the leading causes of failure in betting. Betting 
without a clear plan can result in overspending and unnecessary risk. Set a budget for each bet, 
determine your staking strategy, and stick to it. Betting responsibly is key to long-term success. 
By avoiding these common mistakes, you can make smarter, more calculated bets and increase 
your chances of success when betting on cricket.
"""

        private const val BLOG_TEXT_4 = """
Maximizing Your Cricket Betting Bankroll 
When betting on cricket, managing your bankroll effectively is essential for long-term success. A 
solid bankroll strategy helps you avoid significant losses while maximizing your opportunities to 
profit. Here are some practical tips for maximizing your cricket betting bankroll. 

1. Set a Budget 
Before you start betting, establish a clear budget. Decide how much money you are willing to 
risk and stick to it. This budget should be separate from your personal finances and something 
you're comfortable losing. Setting a budget will prevent you from overspending and help 
maintain control over your betting habits. 

2. Use Flat Betting 
Flat betting is a strategy where you bet the same amount on each wager, regardless of the 
perceived risk or potential reward. This strategy helps you avoid emotional decisions and reduces 
the risk of large losses. It also allows you to manage your bankroll more effectively and keeps 
you in the game longer. 

3. Don’t Bet More Than You Can Afford 
One of the cardinal rules of betting is never to bet more than you can afford to lose. Even if you 
feel confident about a certain bet, always remember that betting is inherently unpredictable. Stick 
to small stakes and never gamble more than what fits within your budget. 

4. Understand the Value of the Bet 
Maximizing your bankroll isn’t just about betting frequently—it’s about making smart bets. 
Always look for value in the odds. This means betting when you believe the odds are favorable 
relative to the likelihood of the outcome. Value betting can increase your long-term profitability, 
even if you don’t win every bet. 

5. Take Advantage of Bonuses and Promotions 
Many betting platforms offer bonuses, free bets, or promotions to attract new users or reward 
loyal players. Take advantage of these offers, but always read the terms and conditions. Using 
bonuses wisely can give you additional betting opportunities without risking more of your own 
money. 

6. Track Your Bets and Performance 
Keep a record of your bets, wins, losses, and overall performance. Tracking your betting history 
helps you identify patterns, recognize what types of bets work for you, and see where you might 
need to improve. It also allows you to spot any tendencies, such as betting on a particular team or 
type of match too often. 

7. Avoid Chasing Losses 
Chasing losses is a quick way to deplete your bankroll. When you lose a bet, it’s important to 
stay disciplined and not try to win back your losses with riskier bets. Stick to your strategy, 
manage your bets within your budget, and avoid making emotional decisions to recover from a 
loss. 

8. Diversify Your Bets 
While it’s tempting to bet on your favorite team or on every match, diversification can help 
mitigate risks. Instead of putting all your money into one type of bet, spread your bets across 
different markets or matches. This gives you more chances to win and helps reduce the impact of 
any one loss on your bankroll. 

9. Use Betting Systems Wisely 
Some bettors like to use betting systems to guide their strategy, such as the Martingale system or 
Kelly Criterion. While these systems can be helpful, they also come with risks. Use betting 
systems carefully and understand their limitations before incorporating them into your betting 
routine. 

By following these tips and focusing on solid bankroll management, you’ll increase your chances 
of making profitable bets while minimizing the risks involved. Maximizing your bankroll is all 
about making disciplined, well-researched decisions and sticking to a betting strategy that suits 
your financial situation.
"""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBlogBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.matchScheduleFragment)
            }
        })

        binding.btnHelp.setOnClickListener {
            findNavController().navigate(R.id.tutorialFragment)
        }
        val cardViews = listOf(
            binding.cardItem1,
            binding.cardItem2,
            binding.cardItem3,
            binding.cardItem4
        )

        cardViews.forEachIndexed { index, card ->
            card.setOnClickListener {
                val item = items[index]
                val action = BlogFragmentDirections.actionBlogFragmentToBlogDetailFragment(
                    title = item.title,
                    text = item.content,
                    imageRes = item.imageRes
                )
                findNavController().navigate(action)
            }
        }
    }
}