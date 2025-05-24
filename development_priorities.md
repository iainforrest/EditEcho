# Edit Echo - Development Priorities & Roadmap

## Current Phase: Product-Founder Fit
**Goal**: Perfect the core experience through Iain's daily usage before external launch

### Immediate Priorities (Next 2-4 weeks)
1. **Fix Notification Persistence Bug** 
   - Currently requires force-close when swiped away
   - Blocks reliable daily usage

2. **Implement Fake Keyboard UI**
   - Position overlay exactly where system keyboard appears
   - Eliminate layout jank that plagues other overlay apps
   - Key differentiator for user experience

3. **Transcribe Only Clipboard Fix**
   - Raw transcriptions aren't auto-copying
   - Breaks consistency with other modes

### Next Phase: Personalization Engine (4-8 weeks)
1. **User Example Upload System**
   - Settings screen for message input
   - Validate and process 10-20 real messages

2. **AI Tone Generation**
   - Use tone_creator.txt prompt with user examples
   - Generate 4 personalized tones with descriptions
   - Replace generic Friendly/Direct/etc. with user-specific tones

3. **Tone Management UI**
   - Edit/rename generated tones
   - Preview tone changes
   - Fallback to defaults if needed

### Business Features (8-12 weeks)
1. **Usage Tracking Integration**
   - SessionLog → DailyUsage pipeline
   - Display usage meters in UI
   - Trigger upgrade prompts at limits

2. **Subscription Logic**
   - Tier enforcement (Standard/Pro/Xtreme)
   - Payment integration
   - Graceful degradation when limits hit

3. **Firebase Integration**
   - Remote configuration
   - Analytics and crash reporting
   - A/B testing for prompts

### Technical Debt & Polish
- **Overlay Architecture Convergence**: Choose XML vs Compose, eliminate dual approach
- **Error Handling**: Robust network failure recovery
- **Performance Optimization**: Battery usage, memory leaks
- **Security**: Encrypted API key storage
- **Testing**: Unit tests for core business logic

## Success Metrics by Phase
**Product-Founder Fit**: Daily usage consistency, time savings validation
**Personalization**: Tone distinctiveness, user preference patterns  
**Business**: Conversion rates, unit economics validation, retention curves

## Risk Mitigation
- **API Costs**: Monitor spend vs usage patterns
- **Android Restrictions**: Stay ahead of permission/overlay policy changes
- **Competition**: Focus on personalization differentiation
- **User Acquisition**: Perfect product before marketing spend