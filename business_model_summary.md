# Edit Echo - Business Model & Unit Economics

## Core Value Proposition
**"Talk it. Tidy it. Send it."** - Transform voice into polished, personalized messages in seconds

### Time Savings Calculation
- **Speaking Speed**: ~150 words per minute
- **Mobile Typing Speed**: ~30 words per minute  
- **Efficiency Ratio**: 4 minutes saved per minute of audio recorded
- **Monthly Impact**: 20 messages/day × 2min audio = 160 minutes saved per week

## Subscription Tiers & Pricing

### Free Tier
- **Price**: $0
- **Features**: Basic transcription, limited usage
- **Purpose**: User acquisition, proof of concept
- **Conversion Strategy**: Show time savings, hit usage limits

### Standard Tier  
- **Price**: $0.99/week (~$4.30/month)
- **Features**: 100 minutes/week recording, tone refinement
- **Target User**: Casual professional, occasional voice messaging
- **Annual Option**: ~$3.01/month (30% discount)

### Pro Tier
- **Price**: $1.99/week (~$8.60/month) 
- **Features**: 4 hours/week transcription, personalized tones
- **Target User**: Heavy voice user, professional communication
- **Annual Option**: ~$6.02/month (30% discount)

### Xtreme Tier
- **Model**: Pay-as-you-go after Pro limit
- **Price**: $1/hour billed per second
- **Requirement**: $10 minimum prepaid balance
- **Target User**: Power users, transcription-heavy workflows

## Unit Economics

### Cost Structure (Per User/Month)
| Component | Standard | Pro | Notes |
|-----------|----------|-----|-------|
| **Whisper API** | $0.36 | $1.44 | $0.006/minute × usage |
| **GPT-4o Refinement** | $0.24 | $0.96 | ~$0.0005 per 500 tokens |
| **Total COGS** | $0.60 | $2.40 | Direct AI costs |

### Revenue & Margins
| Tier | Monthly Revenue | COGS | **Gross Margin** | **Margin %** |
|------|----------------|------|------------------|--------------|
| **Standard** | $4.30 | $0.60 | **$3.70** | **86%** |
| **Pro** | $8.60 | $2.40 | **$6.20** | **72%** |
| **Xtreme** | Variable | ~$0.36/hr | **$0.64/hr** | **64%** |

## Revenue Projections & Scenarios

### Conservative (Year 1)
- **Users**: 1,000 paying subscribers
- **Mix**: 70% Standard, 25% Pro, 5% Xtreme
- **Monthly Revenue**: ~$5,500
- **Annual Revenue**: ~$66,000

### Moderate (Year 2)  
- **Users**: 5,000 paying subscribers
- **Mix**: 60% Standard, 35% Pro, 5% Xtreme
- **Monthly Revenue**: ~$29,000
- **Annual Revenue**: ~$348,000

### Optimistic (Year 3)
- **Users**: 20,000 paying subscribers  
- **Mix**: 50% Standard, 40% Pro, 10% Xtreme
- **Monthly Revenue**: ~$125,000
- **Annual Revenue**: ~$1,500,000

## Key Business Metrics

### Conversion Funnel
- **Free → Standard**: Target 15% conversion rate
- **Standard → Pro**: Target 25% upgrade rate  
- **Churn Rate**: Target <5% monthly (freemium SaaS benchmark)
- **LTV:CAC Ratio**: Target 3:1 minimum

### Usage-Based Triggers
- **Upgrade Prompts**: At 80% of tier limit
- **Value Messaging**: "You've saved 7 hours this month"
- **Limit Notifications**: Graceful degradation, not hard blocks

## Competitive Positioning

### vs. Generic Voice Apps (Otter.ai, etc.)
- **Differentiation**: Personalized tones, mobile-first, instant refinement
- **Price Advantage**: More affordable than enterprise tools
- **UX Advantage**: Fake keyboard overlay, no app switching

### vs. ChatGPT Voice Mode
- **Differentiation**: Specialized for message refinement, persistent overlay
- **Personalization**: Custom tones vs generic responses
- **Efficiency**: Direct voice-to-clipboard vs conversational interface

### vs. Built-in Dictation
- **Value Add**: AI refinement, tone matching, professional polish
- **Convenience**: Overlay access from any app
- **Quality**: Context-aware editing vs raw transcription

## Growth Strategy

### Product-Led Growth
1. **Free Tier**: Demonstrate clear value and time savings
2. **Usage Limits**: Natural upgrade pressure without frustration
3. **Personalization**: Sticky feature that increases switching costs
4. **Word of Mouth**: Visible efficiency gains drive referrals

### Market Expansion
1. **Vertical Focus**: Target neurodivergent users, mobile professionals
2. **Geographic**: English-speaking markets first (US, UK, AU, NZ)
3. **Platform**: Android-first, iOS later based on traction
4. **Enterprise**: Team plans with shared tone libraries (future)

## Risk Mitigation
- **API Cost Control**: Usage monitoring, rate limiting, cost alerts
- **Churn Prevention**: Engagement tracking, personalization stickiness  
- **Competition**: Focus on personalization moat, rapid feature iteration
- **Platform Risk**: Diversify beyond OpenAI, prepare for API changes